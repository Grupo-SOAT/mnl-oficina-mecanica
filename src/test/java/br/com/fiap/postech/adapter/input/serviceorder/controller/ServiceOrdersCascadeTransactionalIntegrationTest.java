package br.com.fiap.postech.adapter.input.serviceorder.controller;

import br.com.fiap.postech.adapter.input.api.model.OwnerRequest;
import br.com.fiap.postech.adapter.input.api.model.ServiceOrderCascadeRequest;
import br.com.fiap.postech.adapter.input.api.model.VehicleCascadeRequest;
import br.com.fiap.postech.adapter.output.owner.persistence.repository.OwnerRepository;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.repository.ServiceOrderRepository;
import br.com.fiap.postech.adapter.output.vehicle.persistence.repository.VehicleRepository;
import br.com.fiap.postech.config.PostgresContainerInitializer;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.vehicle.excecption.DuplicatedVehicleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static br.com.fiap.postech.adapter.input.api.model.DocumentType.CPF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresContainerInitializer.class)
class ServiceOrdersCascadeTransactionalIntegrationTest {

    @Autowired
    private ServiceOrdersController serviceOrdersController;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Test
    public void should_revert_owner_creation_when_vehicle_creation_fails() {
        final var owner = new OwnerRequest()
                .name("Dino da Silva Sauro")
                .document("86781549006")
                .documentType(CPF)
                .phone("123456789")
                .email("dino@sauro.rock");
        final var vehicle = new VehicleCascadeRequest()
                .licensePlate("ABC-1234") // já existe -> duplicata
                .owner(owner)
                .model("Fusca")
                .brand("Volkswagen")
                .color("Azul")
                .year(1970);
        final var serviceOrder = new ServiceOrderCascadeRequest()
                .vehicle(vehicle)
                .description("Essa OS não deve ser criada");

        assertThrows(
                DuplicatedVehicleException.class,
                () -> serviceOrdersController.createServiceOrderCascade(serviceOrder)
        );

        final var createdOwner = ownerRepository.findByDocument(owner.getDocument());
        final var createdServiceOrder = serviceOrderRepository.findAll()
                .stream()
                .filter(so -> so.getDescription().equals(serviceOrder.getDescription()))
                .findFirst();

        assertThat(createdOwner).isEmpty();
        assertThat(createdServiceOrder).isEmpty();
    }

    @Test
    public void should_revert_both_owner_and_vehicle_creations_when_service_order_creation_fails() {
        final var owner = new OwnerRequest()
                .name("Dino da Silva Sauro")
                .document("86781549006")
                .documentType(CPF)
                .phone("123456789")
                .email("dino@sauro.rock");
        final var vehicle = new VehicleCascadeRequest()
                .licensePlate("KJW-9537")
                .owner(owner)
                .model("Fusca")
                .brand("Volkswagen")
                .color("Azul")
                .year(1970);
        final var serviceOrder = new ServiceOrderCascadeRequest()
                .vehicle(vehicle)
                .catalogServiceIds(List.of(99999L)) // serviço inexistente -> falha na criação da OS
                .description("Essa OS não deve ser criada");

        assertThrows(
                CatalogServiceNotFoundException.class,
                () -> serviceOrdersController.createServiceOrderCascade(serviceOrder)
        );

        final var createdOwner = ownerRepository.findByDocument(owner.getDocument());
        final var createdVehicle = vehicleRepository.findByLicensePlate(vehicle.getLicensePlate());

        assertThat(createdOwner).isEmpty();
        assertThat(createdVehicle).isEmpty();
    }

}
