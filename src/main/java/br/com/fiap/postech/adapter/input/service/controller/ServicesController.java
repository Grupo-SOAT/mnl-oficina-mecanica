package br.com.fiap.postech.adapter.input.service.controller;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.adapter.input.api.model.PaginatedServiceResponse;
import br.com.fiap.postech.adapter.input.api.model.ServiceData;
import br.com.fiap.postech.adapter.input.api.model.ServiceRequest;
import br.com.fiap.postech.adapter.input.api.model.ServiceStatus;
import br.com.fiap.postech.adapter.input.service.mapper.ServiceMapper;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.service.exception.NoMatchingServicesException;
import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.usecase.ServiceUseCase;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.port.api.ServicesApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ServicesController implements ServicesApi {

    private final ServiceUseCase serviceUseCase;

    @Override
    public ResponseEntity<PaginatedServiceResponse> listServices(
            Long id,
            Long serviceId,
            ServiceStatus status,
            Integer pageSize,
            String cursor
    ) {
        String statusValue = status != null ? status.getValue() : null;
        final var pageResult = serviceUseCase.scroll(id, serviceId, statusValue, pageSize, cursor);
        final var responseBody = ServiceMapper.toPaginatedResponse(pageResult);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ServiceData> includeService(Long id, ServiceRequest serviceRequest) {
        final var newService = ServiceMapper.fromApiRequest(serviceRequest);
        final var created = serviceUseCase.create(serviceRequest.getServiceOrderId(), newService);
        final var responseBody = ServiceMapper.toApiData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    public ResponseEntity<ServiceData> getServiceById(Long id, Long serviceId) {
        final var service = serviceUseCase.getById(id, serviceId);
        final var responseBody = ServiceMapper.toApiData(service);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<ServiceData> updateService(Long id, Long serviceId, ServiceData serviceData) {
        final var incoming = ServiceMapper.fromApiData(serviceData);
        final var updated = serviceUseCase.update(id, serviceId, incoming);
        final var responseBody = ServiceMapper.toApiData(updated);
        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> deleteService(Long id, Long serviceId) {
        serviceUseCase.delete(id, serviceId);
        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler(NoMatchingServicesException.class)
    public ResponseEntity<Void> handleNoMatchingServices() {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ServiceNotFoundException exception) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(ServiceOrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleServiceOrderNotFound(ServiceOrderNotFoundException exception) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(CatalogServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCatalogServiceNotFound(CatalogServiceNotFoundException exception) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(ServiceUseCase.InvalidServiceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidService(ServiceUseCase.InvalidServiceException exception) {
        final var httpStatus = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(NegativeSupplyQuantityException.class)
    public ResponseEntity<ErrorResponse> handleNegativeSupplyQuantity(NegativeSupplyQuantityException exception) {
        final var httpStatus = HttpStatus.CONFLICT;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }
}
