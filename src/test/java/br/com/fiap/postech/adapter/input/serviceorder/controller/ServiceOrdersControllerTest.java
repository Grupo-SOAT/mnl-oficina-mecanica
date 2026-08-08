package br.com.fiap.postech.adapter.input.serviceorder.controller;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.input.serviceorder.mapper.ServiceOrderMapper;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.serviceorder.exception.*;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderCascadeCreationCommand;
import br.com.fiap.postech.domain.serviceorder.usecase.ChangeServiceOrderStatusUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.CreateServiceOrderCascadeUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.ServiceOrderUseCase;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleOwnerDataAbsentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus.DELIVERED;
import static br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceOrdersControllerTest {

    @Mock
    private ServiceOrderUseCase serviceOrderUseCase;

    @Mock
    private ChangeServiceOrderStatusUseCase changeStatusUseCase;

    @Mock
    private CreateServiceOrderCascadeUseCase createServiceOrderCascadeUseCase;

    @InjectMocks
    private ServiceOrdersController controller;

    @Test
    void should_create_service_order_cascade_and_return_created_status() {
        var request = new ServiceOrderCascadeRequest();
        request.setOwnerId(1L);
        request.setVehicleId(10L);
        request.setDescription("Complete service");
        request.setCatalogServiceIds(List.of(1L, 2L));

        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(5L)
                .clientId(1L)
                .vehicleId(10L)
                .description("Complete service")
                .status("PENDING")
                .build();

        var responseData = new ServiceOrderData()
                .id(5L)
                .clientId(1L)
                .vehicleId(10L)
                .description("Complete service")
                .status(ServiceOrderStatus.PENDING)
                .statusLabel("Recebida");

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            var command = mock(ServiceOrderCascadeCreationCommand.class);
            mapper.when(() -> ServiceOrderMapper.buildCascadeCreationCommand(request))
                    .thenReturn(command);
            when(createServiceOrderCascadeUseCase.execute(command))
                    .thenReturn(createdServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(createdServiceOrder))
                    .thenReturn(responseData);

            var response = controller.createServiceOrderCascade(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(responseData);
            assertThat(response.getBody().getStatusLabel()).isEqualTo("Recebida");
        }
    }

    @Test
    void should_call_mapper_to_build_cascade_creation_command() {
        var request = new ServiceOrderCascadeRequest();
        request.setOwnerId(2L);
        request.setVehicleId(12L);

        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(6L)
                .clientId(2L)
                .vehicleId(12L)
                .build();

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            var command = mock(ServiceOrderCascadeCreationCommand.class);
            mapper.when(() -> ServiceOrderMapper.buildCascadeCreationCommand(request))
                    .thenReturn(command);
            when(createServiceOrderCascadeUseCase.execute(command))
                    .thenReturn(createdServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(createdServiceOrder))
                    .thenReturn(new ServiceOrderData());

            controller.createServiceOrderCascade(request);

            mapper.verify(() -> ServiceOrderMapper.buildCascadeCreationCommand(request));
        }
    }

    @Test
    void should_call_use_case_with_cascade_creation_command() {
        var request = new ServiceOrderCascadeRequest();
        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(7L)
                .clientId(3L)
                .vehicleId(15L)
                .build();

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            var command = mock(ServiceOrderCascadeCreationCommand.class);
            mapper.when(() -> ServiceOrderMapper.buildCascadeCreationCommand(request))
                    .thenReturn(command);
            when(createServiceOrderCascadeUseCase.execute(command))
                    .thenReturn(createdServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(createdServiceOrder))
                    .thenReturn(new ServiceOrderData());

            controller.createServiceOrderCascade(request);

            verify(createServiceOrderCascadeUseCase).execute(command);
        }
    }

    @Test
    void should_map_created_service_order_to_response_data() {
        var request = new ServiceOrderCascadeRequest();
        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(8L)
                .clientId(4L)
                .vehicleId(20L)
                .build();
        var responseData = new ServiceOrderData()
                .id(8L)
                .clientId(4L)
                .vehicleId(20L);

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            var command = mock(ServiceOrderCascadeCreationCommand.class);
            mapper.when(() -> ServiceOrderMapper.buildCascadeCreationCommand(request))
                    .thenReturn(command);
            when(createServiceOrderCascadeUseCase.execute(command))
                    .thenReturn(createdServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(createdServiceOrder))
                    .thenReturn(responseData);

            var response = controller.createServiceOrderCascade(request);

            mapper.verify(() -> ServiceOrderMapper.toApiData(createdServiceOrder));
            assertThat(response.getBody()).isEqualTo(responseData);
        }
    }

    @Test
    void should_handle_service_order_vehicle_data_absent_exception() {
        var exception = new ServiceOrderVehicleDataAbsentException();
        var response = controller.handleServiceOrderVehicleDataAbsentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @Test
    void should_handle_vehicle_owner_data_absent_exception() {
        var exception = new VehicleOwnerDataAbsentException();
        var response = controller.handleVehicleOwnerDataAbsentException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @Test
    void should_return_created_service_order_data_in_response_body() {
        var request = new ServiceOrderCascadeRequest();
        request.setOwnerId(5L);
        request.setVehicleId(25L);
        request.setDescription("Full service with cascade");

        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(9L)
                .clientId(5L)
                .vehicleId(25L)
                .description("Full service with cascade")
                .status("PENDING")
                .build();

        var expectedResponse = new ServiceOrderData()
                .id(9L)
                .clientId(5L)
                .vehicleId(25L)
                .description("Full service with cascade")
                .status(ServiceOrderStatus.PENDING)
                .statusLabel("Recebida");

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            var command = mock(ServiceOrderCascadeCreationCommand.class);
            mapper.when(() -> ServiceOrderMapper.buildCascadeCreationCommand(request))
                    .thenReturn(command);
            when(createServiceOrderCascadeUseCase.execute(command))
                    .thenReturn(createdServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(createdServiceOrder))
                    .thenReturn(expectedResponse);

            var response = controller.createServiceOrderCascade(request);

            assertThat(response.getBody()).isSameAs(expectedResponse);
            assertThat(response.getBody().getId()).isEqualTo(9L);
            assertThat(response.getBody().getClientId()).isEqualTo(5L);
            assertThat(response.getBody().getStatusLabel()).isEqualTo("Recebida");
        }
    }
    @Test
    void should_list_service_orders_with_status_filter() {
        var pageResult = mock(PaginatedServiceOrderResponse.class);
        var responseData = new PaginatedServiceOrderResponse();

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            when(serviceOrderUseCase.scroll("PENDING", 1L, 10L, 5, "cursor1"))
                    .thenReturn(mock(ScrollPage.class));
            mapper.when(() -> ServiceOrderMapper.toPaginatedResponse(any()))
                    .thenReturn(responseData);

            var response = controller.listServiceOrders(ServiceOrderStatus.PENDING, 1L, 10L, 5, "cursor1");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(responseData);
        }
    }

    @Test
    void should_list_service_orders_without_status_filter() {
        var responseData = new PaginatedServiceOrderResponse();

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            when(serviceOrderUseCase.scroll(null, 2L, 12L, 10, "cursor2"))
                    .thenReturn(mock(ScrollPage.class));
            mapper.when(() -> ServiceOrderMapper.toPaginatedResponse(any()))
                    .thenReturn(responseData);

            var response = controller.listServiceOrders(null, 2L, 12L, 10, "cursor2");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void should_create_service_order_and_return_created_status() {
        var request = new ServiceOrderRequest();
        request.setClientId(1L);
        request.setVehicleId(10L);
        request.setDescription("Standard service");

        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .clientId(1L)
                .vehicleId(10L)
                .description("Standard service")
                .build();

        var responseData = new ServiceOrderData()
                .id(1L)
                .clientId(1L)
                .vehicleId(10L);

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            mapper.when(() -> ServiceOrderMapper.fromApiRequest(request))
                    .thenReturn(createdServiceOrder);
            when(serviceOrderUseCase.create(createdServiceOrder))
                    .thenReturn(createdServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(createdServiceOrder))
                    .thenReturn(responseData);

            var response = controller.createServiceOrder(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isEqualTo(responseData);
        }
    }

    @Test
    void should_get_service_order_by_id() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(5L)
                .clientId(1L)
                .vehicleId(10L)
                .build();

        var responseData = new ServiceOrderData()
                .id(5L)
                .clientId(1L)
                .vehicleId(10L);

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            when(serviceOrderUseCase.getById(5L))
                    .thenReturn(serviceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(serviceOrder))
                    .thenReturn(responseData);

            var response = controller.getServiceOrderById(5L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(responseData);
        }
    }

    @Test
    void should_update_service_order() {
        var updateData = new ServiceOrderData()
                .id(3L)
                .clientId(2L)
                .vehicleId(15L);

        var updatedServiceOrder = ServiceOrderEntity.builder()
                .id(3L)
                .clientId(2L)
                .vehicleId(15L)
                .build();

        var responseData = new ServiceOrderData()
                .id(3L)
                .clientId(2L)
                .vehicleId(15L);

        try (MockedStatic<ServiceOrderMapper> mapper = mockStatic(ServiceOrderMapper.class)) {
            mapper.when(() -> ServiceOrderMapper.fromApiData(updateData))
                    .thenReturn(updatedServiceOrder);
            when(serviceOrderUseCase.update(3L, updatedServiceOrder))
                    .thenReturn(updatedServiceOrder);
            mapper.when(() -> ServiceOrderMapper.toApiData(updatedServiceOrder))
                    .thenReturn(responseData);

            var response = controller.updateServiceOrder(3L, updateData);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(responseData);
        }
    }

    @Test
    void should_delete_service_order() {
        var response = controller.deleteServiceOrder(4L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(serviceOrderUseCase).delete(4L);
    }

    @Test
    void should_register_progress_and_return_accepted() {
        var request = new ServiceOrderActionRequest();
        request.setAction(ServiceOrderAction.START_INSPECTION);
        request.setRelatedServiceId(null);

        var response = controller.registerProgress(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(changeStatusUseCase).registerProgress(1L, ServiceOrderAction.START_INSPECTION, null);
    }

    @Test
    void should_register_progress_with_related_service_id() {
        var request = new ServiceOrderActionRequest();
        request.setAction(ServiceOrderAction.COMPLETE_INSPECTION);
        request.setRelatedServiceId(10L);

        var response = controller.registerProgress(2L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(changeStatusUseCase).registerProgress(2L, ServiceOrderAction.COMPLETE_INSPECTION, 10L);
    }

    @Test
    void should_register_client_decision_and_return_accepted() {
        var request = new BudgetDecisionRequest();
        request.setDecision(BudgetDecision.APPROVE);

        var response = controller.registerClientDecision(3L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(changeStatusUseCase).registerClientDecision(3L, BudgetDecision.APPROVE);
    }

    @Test
    void should_handle_no_matching_service_orders() {
        var response = controller.handleNoMatchingServiceOrders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_handle_service_order_not_found_exception() {
        var exception = new ServiceOrderNotFoundException(1L);
        var response = controller.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(404);
    }

    @Test
    void should_handle_service_order_client_not_found_exception() {
        var exception = new ServiceOrderClientNotFoundException(2L);
        var response = controller.handleClientNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @Test
    void should_handle_service_order_vehicle_not_found_exception() {
        var exception = new ServiceOrderVehicleNotFoundException(3L);
        var response = controller.handleVehicleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(400);
    }

    @Test
    void should_handle_status_change_not_allowed_exception() {
        var exception = new StatusChangeNotAllowedException(PENDING, DELIVERED);
        var response = controller.handleStatusChangeNotAllowed(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(409);
        assertThat(response.getBody().getReason()).isEqualTo("STATUS_CHANGE_NOT_ALLOWED");
    }

    @Test
    void should_handle_partial_budget_rejection_not_implemented_exception() {
        var exception = new PartialBudgetRejectionNotImplementedException();
        var response = controller.handlePartialBudgetRejectionNotImplemented(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(501);
        assertThat(response.getBody().getReason()).isEqualTo("PARTIAL_BUDGET_REJECTION_NOT_IMPLEMENTED");
    }

    @Test
    void should_handle_negative_supply_quantity_exception() {
        var exception = new NegativeSupplyQuantityException(-1L);
        var response = controller.handleNegativeSupplyQuantity(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(409);
    }

}
