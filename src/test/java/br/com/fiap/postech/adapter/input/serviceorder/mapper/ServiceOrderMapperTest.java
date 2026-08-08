package br.com.fiap.postech.adapter.input.serviceorder.mapper;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.input.vehicle.mapper.VehicleMapper;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderCascadeCreationCommand;
import br.com.fiap.postech.domain.vehicle.model.VehicleCascadeCreationCommand;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ServiceOrderMapperTest {

    @Test
    void should_map_from_api_request_to_service_order() {
        ServiceOrderRequest request = new ServiceOrderRequest()
                .clientId(1L)
                .vehicleId(10L)
                .description("Oil change and filter replacement");

        ServiceOrder result = ServiceOrderMapper.fromApiRequest(request);

        assertThat(result).isInstanceOf(ServiceOrderEntity.class);
        assertThat(result.getClientId()).isEqualTo(1L);
        assertThat(result.getVehicleId()).isEqualTo(10L);
        assertThat(result.getDescription()).isEqualTo("Oil change and filter replacement");
    }

    @Test
    void should_map_from_api_data_to_service_order() {
        ServiceOrderData data = new ServiceOrderData()
                .id(5L)
                .clientId(2L)
                .vehicleId(12L)
                .description("Brake inspection")
                .status(ServiceOrderStatus.PENDING)
                .estimatedAmount(new BigDecimal("250.00"));

        ServiceOrder result = ServiceOrderMapper.fromApiData(data);

        assertThat(result).isInstanceOf(ServiceOrderEntity.class);
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getClientId()).isEqualTo(2L);
        assertThat(result.getVehicleId()).isEqualTo(12L);
        assertThat(result.getDescription()).isEqualTo("Brake inspection");
        assertThat(result.getEstimatedAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void should_map_from_api_data_with_null_status() {
        ServiceOrderData data = new ServiceOrderData()
                .id(3L)
                .clientId(1L)
                .vehicleId(10L)
                .description("Maintenance")
                .status(null);

        ServiceOrder result = ServiceOrderMapper.fromApiData(data);

        assertThat(result.getStatus()).isNull();
    }

    @Test
    void should_map_service_order_to_api_data() {
        LocalDateTime now = LocalDateTime.now();
        ServiceOrder serviceOrder = ServiceOrderEntity.builder()
                .id(7L)
                .clientId(3L)
                .vehicleId(15L)
                .description("Complete inspection")
                .status("PENDING")
                .estimatedAmount(new BigDecimal("500.00"))
                .createdAt(now)
                .updatedAt(now)
                .build();

        ServiceOrderData result = ServiceOrderMapper.toApiData(serviceOrder);

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getClientId()).isEqualTo(3L);
        assertThat(result.getVehicleId()).isEqualTo(15L);
        assertThat(result.getDescription()).isEqualTo("Complete inspection");
        assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.PENDING);
        assertThat(result.getEstimatedAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_map_service_order_with_all_timestamps_to_api_data() {
        LocalDateTime now = LocalDateTime.now();
        ServiceOrder serviceOrder = ServiceOrderEntity.builder()
                .id(8L)
                .clientId(4L)
                .vehicleId(16L)
                .description("Full service")
                .status("COMPLETED")
                .estimatedAmount(new BigDecimal("750.00"))
                .createdAt(now)
                .updatedAt(now)
                .inspectedAt(now)
                .approvedAt(now)
                .startedAt(now)
                .completedAt(now)
                .deliveredAt(now)
                .build();

        ServiceOrderData result = ServiceOrderMapper.toApiData(serviceOrder);

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        assertThat(result.getInspectedAt()).isNotNull();
        assertThat(result.getApprovedAt()).isNotNull();
        assertThat(result.getStartedAt()).isNotNull();
        assertThat(result.getCompletedAt()).isNotNull();
        assertThat(result.getDeliveredAt()).isNotNull();
    }

    @Test
    void should_map_service_order_with_null_status_to_api_data() {
        ServiceOrder serviceOrder = ServiceOrderEntity.builder()
                .id(9L)
                .clientId(5L)
                .vehicleId(17L)
                .description("Service")
                .status(null)
                .estimatedAmount(new BigDecimal("100.00"))
                .build();

        ServiceOrderData result = ServiceOrderMapper.toApiData(serviceOrder);

        assertThat(result.getStatus()).isNull();
    }

    @Test
    void should_map_scroll_page_to_paginated_response() {
        ScrollPage<ServiceOrder> page = ScrollPage.<ServiceOrder>builder()
                .data(List.of(
                        ServiceOrderEntity.builder().id(1L).clientId(1L).vehicleId(10L).description("Service 1").build(),
                        ServiceOrderEntity.builder().id(2L).clientId(2L).vehicleId(11L).description("Service 2").build()
                ))
                .cursor("2")
                .pageSize(2)
                .isLast(false)
                .build();

        PaginatedServiceOrderResponse result = ServiceOrderMapper.toPaginatedResponse(page);

        assertThat(result).isNotNull();
        assertThat(result.getPageSize()).isEqualTo(2);
        assertThat(result.getCursor()).isEqualTo("2");
        assertThat(result.getIsLast()).isFalse();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).getId()).isEqualTo(1L);
        assertThat(result.getData().get(1).getId()).isEqualTo(2L);
    }

    @Test
    void should_build_cascade_creation_command_with_vehicle_and_services() {
        VehicleCascadeRequest vehicleRequest = mock(VehicleCascadeRequest.class);
        VehicleCascadeCreationCommand vehicleCascadeCommand = mock(VehicleCascadeCreationCommand.class);

        ServiceOrderCascadeRequest request = new ServiceOrderCascadeRequest()
                .ownerId(1L)
                .vehicleId(10L)
                .description("Complete service")
                .vehicle(vehicleRequest)
                .catalogServiceIds(List.of(1L, 2L));

        try (MockedStatic<VehicleMapper> vehicleMapperMock = mockStatic(VehicleMapper.class)) {

            vehicleMapperMock.when(() -> VehicleMapper.buildCascadeCreationCommand(vehicleRequest))
                    .thenReturn(vehicleCascadeCommand);

            ServiceOrderCascadeCreationCommand result = ServiceOrderMapper.buildCascadeCreationCommand(request);

            assertThat(result).isNotNull();
            assertThat(result.vehicleCascadeCreationCommand()).isSameAs(vehicleCascadeCommand);
            assertThat(result.serviceOrder()).isNotNull();
            assertThat(result.serviceOrder().getClientId()).isEqualTo(1L);
            assertThat(result.serviceOrder().getVehicleId()).isEqualTo(10L);
            assertThat(result.serviceOrder().getDescription()).isEqualTo("Complete service");
            assertThat(result.catalogServiceIds()).containsExactly(1L, 2L);
        }
    }

    @Test
    void should_build_cascade_creation_command_without_vehicle() {
        ServiceOrderCascadeRequest request = new ServiceOrderCascadeRequest()
                .ownerId(2L)
                .vehicleId(11L)
                .description("Service without vehicle cascade")
                .vehicle(null)
                .catalogServiceIds(List.of(3L));

        ServiceOrderCascadeCreationCommand result = ServiceOrderMapper.buildCascadeCreationCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.vehicleCascadeCreationCommand()).isNull();
        assertThat(result.serviceOrder()).isNotNull();
        assertThat(result.serviceOrder().getClientId()).isEqualTo(2L);
        assertThat(result.catalogServiceIds()).containsExactly(3L);
    }

    @Test
    void should_build_cascade_creation_command_without_services() {
        VehicleCascadeRequest vehicleRequest = mock(VehicleCascadeRequest.class);
        VehicleCascadeCreationCommand vehicleCascadeCommand = mock(VehicleCascadeCreationCommand.class);

        ServiceOrderCascadeRequest request = new ServiceOrderCascadeRequest()
                .ownerId(3L)
                .vehicleId(12L)
                .description("Service without catalog services")
                .vehicle(vehicleRequest)
                .catalogServiceIds(null);

        try (MockedStatic<VehicleMapper> vehicleMapperMock = mockStatic(VehicleMapper.class)) {

            vehicleMapperMock.when(() -> VehicleMapper.buildCascadeCreationCommand(vehicleRequest))
                    .thenReturn(vehicleCascadeCommand);

            ServiceOrderCascadeCreationCommand result = ServiceOrderMapper.buildCascadeCreationCommand(request);

            assertThat(result).isNotNull();
            assertThat(result.vehicleCascadeCreationCommand()).isSameAs(vehicleCascadeCommand);
            assertThat(result.catalogServiceIds()).isEmpty();
        }
    }

    @Test
    void should_build_cascade_creation_command_without_vehicle_and_services() {
        ServiceOrderCascadeRequest request = new ServiceOrderCascadeRequest()
                .ownerId(4L)
                .vehicleId(13L)
                .description("Simple service order")
                .vehicle(null)
                .catalogServiceIds(null);

        ServiceOrderCascadeCreationCommand result = ServiceOrderMapper.buildCascadeCreationCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.vehicleCascadeCreationCommand()).isNull();
        assertThat(result.serviceOrder()).isNotNull();
        assertThat(result.serviceOrder().getClientId()).isEqualTo(4L);
        assertThat(result.catalogServiceIds()).isEmpty();
    }

    @Test
    void should_build_cascade_creation_command_with_empty_catalog_service_ids() {
        ServiceOrderCascadeRequest request = new ServiceOrderCascadeRequest()
                .ownerId(5L)
                .vehicleId(14L)
                .description("Service with empty catalog list")
                .vehicle(null)
                .catalogServiceIds(new ArrayList<>());

        ServiceOrderCascadeCreationCommand result = ServiceOrderMapper.buildCascadeCreationCommand(request);

        assertThat(result).isNotNull();
        assertThat(result.catalogServiceIds()).isEmpty();
    }

    @Test
    void should_convert_timestamps_to_utc_offset() {
        LocalDateTime localTime = LocalDateTime.of(2024, 6, 4, 10, 30, 0);
        ServiceOrder serviceOrder = ServiceOrderEntity.builder()
                .id(10L)
                .clientId(6L)
                .vehicleId(18L)
                .description("UTC test")
                .createdAt(localTime)
                .build();

        ServiceOrderData result = ServiceOrderMapper.toApiData(serviceOrder);

        OffsetDateTime expectedOffset = localTime.atOffset(ZoneOffset.UTC);
        assertThat(result.getCreatedAt()).isEqualTo(expectedOffset);
    }

}

