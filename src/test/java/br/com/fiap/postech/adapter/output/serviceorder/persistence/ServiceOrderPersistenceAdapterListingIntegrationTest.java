package br.com.fiap.postech.adapter.output.serviceorder.persistence;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.owner.persistence.repository.OwnerRepository;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.repository.ServiceOrderRepository;
import br.com.fiap.postech.adapter.output.vehicle.persistence.entity.VehicleEntity;
import br.com.fiap.postech.adapter.output.vehicle.persistence.repository.VehicleRepository;
import br.com.fiap.postech.config.PostgresContainerInitializer;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates the default service order listing (no status filter) against a real Postgres instance,
 * since the status-priority ordering and exclusion rule are expressed as JPA Criteria/SQL that a
 * mocked repository cannot exercise.
 */
@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresContainerInitializer.class)
class ServiceOrderPersistenceAdapterListingIntegrationTest {

    @Autowired
    private ServiceOrderPersistencePort persistencePort;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Test
    void should_order_default_listing_by_status_priority_and_exclude_completed_and_delivered() {
        Long clientId = createOwner("priority-order");
        Long vehicleId = createVehicle(clientId, "priority-order");

        saveOrder(clientId, vehicleId, "COMPLETED");
        saveOrder(clientId, vehicleId, "DELIVERED");
        saveOrder(clientId, vehicleId, "APPROVED");
        saveOrder(clientId, vehicleId, "CANCELLED");
        saveOrder(clientId, vehicleId, "PARTIALLY_REJECTED");
        saveOrder(clientId, vehicleId, "PENDING");
        saveOrder(clientId, vehicleId, "IN_INSPECTION");
        saveOrder(clientId, vehicleId, "AWAITING_APPROVAL");
        saveOrder(clientId, vehicleId, "IN_PROGRESS");

        var page = persistencePort.scroll(null, clientId, vehicleId, 20, null);

        assertThat(statusesOf(page.data())).containsExactly(
                "IN_PROGRESS",
                "AWAITING_APPROVAL",
                "IN_INSPECTION",
                "PENDING",
                "PARTIALLY_REJECTED",
                "CANCELLED",
                "APPROVED"
        );
    }

    @Test
    void should_order_orders_with_same_status_by_id_ascending() {
        Long clientId = createOwner("tie-break");
        Long vehicleId = createVehicle(clientId, "tie-break");

        Long olderId = saveOrder(clientId, vehicleId, "PENDING").getId();
        Long newerId = saveOrder(clientId, vehicleId, "PENDING").getId();

        var page = persistencePort.scroll(null, clientId, vehicleId, 20, null);

        assertThat(idsOf(page.data())).containsExactly(olderId, newerId);
    }

    @Test
    void should_paginate_default_listing_across_priority_buckets_using_composite_cursor() {
        Long clientId = createOwner("composite-cursor");
        Long vehicleId = createVehicle(clientId, "composite-cursor");

        Long pendingId = saveOrder(clientId, vehicleId, "PENDING").getId();
        Long firstInProgressId = saveOrder(clientId, vehicleId, "IN_PROGRESS").getId();
        Long secondInProgressId = saveOrder(clientId, vehicleId, "IN_PROGRESS").getId();

        var firstPage = persistencePort.scroll(null, clientId, vehicleId, 2, null);
        assertThat(idsOf(firstPage.data())).containsExactly(firstInProgressId, secondInProgressId);
        assertThat(firstPage.isLast()).isFalse();

        var secondPage = persistencePort.scroll(null, clientId, vehicleId, 2, firstPage.cursor());
        assertThat(idsOf(secondPage.data())).containsExactly(pendingId);
        assertThat(secondPage.isLast()).isTrue();
    }

    private List<String> statusesOf(List<ServiceOrder> orders) {
        return orders.stream().map(ServiceOrder::getStatus).toList();
    }

    private List<Long> idsOf(List<ServiceOrder> orders) {
        return orders.stream().map(ServiceOrder::getId).toList();
    }

    private ServiceOrderEntity saveOrder(Long clientId, Long vehicleId, String status) {
        return serviceOrderRepository.save(ServiceOrderEntity.builder()
                .clientId(clientId)
                .vehicleId(vehicleId)
                .description("OS para teste de listagem")
                .status(status)
                .estimatedAmount(BigDecimal.TEN)
                .build());
    }

    private Long createOwner(String suffix) {
        String unique = suffix + "-" + System.nanoTime();
        return ownerRepository.save(OwnerEntity.builder()
                .name("Cliente " + unique)
                .document(unique)
                .documentType(DocumentType.CPF)
                .phone("11999999999")
                .email(unique + "@teste.com")
                .build()).getId();
    }

    private Long createVehicle(Long ownerId, String suffix) {
        String unique = suffix + "-" + System.nanoTime();
        return vehicleRepository.save(VehicleEntity.builder()
                .ownerId(ownerId)
                .licensePlate(unique)
                .brand("Marca")
                .model("Modelo")
                .year(2024)
                .color("Branco")
                .build()).getId();
    }
}
