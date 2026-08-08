package br.com.fiap.postech.adapter.input.catalogservice.controller;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.input.catalogservice.mapper.CatalogServicesMapper;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.catalogservices.exception.DuplicatedCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServiceNameException;
import br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServicePriceException;
import br.com.fiap.postech.domain.catalogservices.exception.InvalidSupplyQuantityException;
import br.com.fiap.postech.domain.catalogservices.exception.NoMatchingCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.usecase.CatalogServicesUseCase;
import br.com.fiap.postech.port.api.CatalogServicesApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CatalogServicesController implements CatalogServicesApi {
    private final CatalogServicesUseCase catalogServicesUseCase;

    @Override
    public ResponseEntity<PaginatedCatalogServiceResponse> listCatalogServices(Long id, String name, Integer pageSize, String cursor) {
        final var pageResult = catalogServicesUseCase.scroll(id, name, pageSize, cursor);
        final var responseBody = CatalogServicesMapper.toPaginatedResponse(pageResult);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<CatalogServiceData> createCatalogService(CatalogServiceRequest catalogServiceRequest){
        final var newCatalogService = CatalogServicesMapper.fromApiRequest(catalogServiceRequest);
        final var created = catalogServicesUseCase.create(newCatalogService);

        final var responseBody = CatalogServicesMapper.toApiData(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    public ResponseEntity<CatalogServiceData> updateCatalogService(Long id, CatalogServiceData catalogServiceData) {
        final var existingCatalogService = CatalogServicesMapper.fromApiData(catalogServiceData);
        final var updated = catalogServicesUseCase.update(id, existingCatalogService);
        final var responseBody = CatalogServicesMapper.toApiData(updated);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> deleteCatalogService(Long id) {
        catalogServicesUseCase.delete(id);

        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<CatalogServiceData> getCatalogServiceById(Long id) {
        var catalogService = catalogServicesUseCase.getById(id);
        final var responseBody = CatalogServicesMapper.toApiData(catalogService);

        return ResponseEntity.ok(responseBody);
    }

    @ExceptionHandler(NoMatchingCatalogServiceException.class)
    public ResponseEntity<Void> handleNoMatchingCatalogServices(NoMatchingCatalogServiceException exception) {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(CatalogServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CatalogServiceNotFoundException exception) {
        final var status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(DuplicatedCatalogServiceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicated(DuplicatedCatalogServiceException exception) {
        final var status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler({
            InvalidCatalogServiceNameException.class,
            InvalidCatalogServicePriceException.class,
            InvalidSupplyQuantityException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException exception) {
        final var status = HttpStatus.BAD_REQUEST;
        String reason = switch (exception) {
            case InvalidCatalogServiceNameException ex -> ex.reason.name();
            case InvalidCatalogServicePriceException ex -> ex.reason.name();
            case InvalidSupplyQuantityException ex -> ex.reason.name();
            default -> "BAD_REQUEST";
        };
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), reason, exception.getMessage()));
    }
}
