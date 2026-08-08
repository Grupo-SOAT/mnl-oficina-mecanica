package br.com.fiap.postech.adapter.input.serviceorder.controller;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.input.serviceorder.mapper.ServiceOrderMapper;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.serviceorder.exception.*;
import br.com.fiap.postech.domain.serviceorder.usecase.ChangeServiceOrderStatusUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.CreateServiceOrderCascadeUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.ServiceOrderUseCase;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleOwnerDataAbsentException;
import br.com.fiap.postech.port.api.ServiceOrdersApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ServiceOrdersController implements ServiceOrdersApi {

    private final ServiceOrderUseCase serviceOrderUseCase;
    private final ChangeServiceOrderStatusUseCase changeStatusUseCase;
    private final CreateServiceOrderCascadeUseCase createServiceOrderCascadeUseCase;

    @Override
    public ResponseEntity<PaginatedServiceOrderResponse> listServiceOrders(
            ServiceOrderStatus status,
            Long clientId,
            Long vehicleId,
            Integer pageSize,
            String cursor
    ) {
        String statusValue = status != null ? status.getValue() : null;
        final var pageResult = serviceOrderUseCase.scroll(statusValue, clientId, vehicleId, pageSize, cursor);
        final var responseBody = ServiceOrderMapper.toPaginatedResponse(pageResult);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ServiceOrderData> createServiceOrder(ServiceOrderRequest serviceOrderRequest) {
        final var newServiceOrder = ServiceOrderMapper.fromApiRequest(serviceOrderRequest);
        final var created = serviceOrderUseCase.create(newServiceOrder);
        final var responseBody = ServiceOrderMapper.toApiData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    @Transactional
    public ResponseEntity<ServiceOrderData> createServiceOrderCascade(ServiceOrderCascadeRequest serviceOrderCascadeRequest) {
        final var serviceOrderCascadeCreationCommand =
                ServiceOrderMapper.buildCascadeCreationCommand(serviceOrderCascadeRequest);
        final var created = createServiceOrderCascadeUseCase.execute(serviceOrderCascadeCreationCommand);
        final var responseBody = ServiceOrderMapper.toApiData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    public ResponseEntity<ServiceOrderData> getServiceOrderById(Long id) {
        final var serviceOrder = serviceOrderUseCase.getById(id);
        final var responseBody = ServiceOrderMapper.toApiData(serviceOrder);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ServiceOrderData> updateServiceOrder(Long id, ServiceOrderData serviceOrderData) {
        final var incoming = ServiceOrderMapper.fromApiData(serviceOrderData);
        final var updated = serviceOrderUseCase.update(id, incoming);
        final var responseBody = ServiceOrderMapper.toApiData(updated);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> deleteServiceOrder(Long id) {
        serviceOrderUseCase.delete(id);
        return ResponseEntity.accepted().build();
    }

    @Override
    @Transactional
    public ResponseEntity<Void> registerProgress(Long id, ServiceOrderActionRequest serviceOrderActionRequest) {
        changeStatusUseCase.registerProgress(
                id,
                serviceOrderActionRequest.getAction(),
                serviceOrderActionRequest.getRelatedServiceId()
        );
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> registerClientDecision(Long id, BudgetDecisionRequest budgetDecisionRequest) {
        changeStatusUseCase.registerClientDecision(id, budgetDecisionRequest.getDecision());
        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler(NoMatchingServiceOrdersException.class)
    public ResponseEntity<Void> handleNoMatchingServiceOrders() {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ServiceOrderNotFoundException exception) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(ServiceOrderClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFound(ServiceOrderClientNotFoundException exception) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(ServiceOrderVehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVehicleNotFound(ServiceOrderVehicleNotFoundException exception) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(StatusChangeNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleStatusChangeNotAllowed(StatusChangeNotAllowedException exception) {
        final var httpStatus = HttpStatus.CONFLICT;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), "STATUS_CHANGE_NOT_ALLOWED", exception.getMessage()));
    }

    @ExceptionHandler(PartialBudgetRejectionNotImplementedException.class)
    public ResponseEntity<ErrorResponse> handlePartialBudgetRejectionNotImplemented(PartialBudgetRejectionNotImplementedException exception) {
        final var httpStatus = HttpStatus.NOT_IMPLEMENTED;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), "PARTIAL_BUDGET_REJECTION_NOT_IMPLEMENTED", exception.getMessage()));
    }

    @ExceptionHandler(NegativeSupplyQuantityException.class)
    public ResponseEntity<ErrorResponse> handleNegativeSupplyQuantity(NegativeSupplyQuantityException exception) {
        final var httpStatus = HttpStatus.CONFLICT;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(ServiceOrderVehicleDataAbsentException.class)
    public ResponseEntity<ErrorResponse> handleServiceOrderVehicleDataAbsentException(
            ServiceOrderVehicleDataAbsentException exception
    ) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(CatalogServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCatalogServiceNotFoundException(
            CatalogServiceNotFoundException exception
    ) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(VehicleOwnerDataAbsentException.class)
    public ResponseEntity<ErrorResponse> handleVehicleOwnerDataAbsentException(
            VehicleOwnerDataAbsentException exception
    ) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }
}
