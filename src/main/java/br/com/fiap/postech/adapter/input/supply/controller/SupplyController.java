package br.com.fiap.postech.adapter.input.supply.controller;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.adapter.input.api.model.PaginatedSupplyResponse;
import br.com.fiap.postech.adapter.input.api.model.SupplyData;
import br.com.fiap.postech.adapter.input.api.model.SupplyRequest;
import br.com.fiap.postech.adapter.input.supply.mapper.SupplyMapper;
import br.com.fiap.postech.domain.supply.exception.DuplicatedSupplyException;
import br.com.fiap.postech.domain.supply.exception.NoMatchingSuppliesException;
import br.com.fiap.postech.domain.supply.exception.SupplyNotFoundException;
import br.com.fiap.postech.domain.supply.usecase.SupplyUseCase;
import br.com.fiap.postech.port.api.SuppliesApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SupplyController implements SuppliesApi {
    private final SupplyUseCase supplyUseCase;

    @Override
    public ResponseEntity<PaginatedSupplyResponse> listSupplies(String sku, Integer pageSize, String cursor) {
        final var pageResult = supplyUseCase.scroll(sku, pageSize, cursor);
        final var responseBody = SupplyMapper.toPaginatedResponse(pageResult);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<SupplyData> getSupplyById(Long id) {
        final var supply = supplyUseCase.getById(id);
        final var responseBody = SupplyMapper.toApiData(supply);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<SupplyData> createSupply(SupplyRequest supplyRequest) {
        final var newSupply = SupplyMapper.fromApiRequest(supplyRequest);
        final var created = supplyUseCase.create(newSupply);
        final var responseBody = SupplyMapper.toApiData(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    public ResponseEntity<SupplyData> updateSupply(Long id, SupplyData supplyData) {
        final var existingSupply = SupplyMapper.fromApiData(supplyData);
        final var updated = supplyUseCase.update(id, existingSupply);
        final var responseBody = SupplyMapper.toApiData(updated);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> deleteSupply(Long id) {
        supplyUseCase.delete(id);

        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler(NoMatchingSuppliesException.class)
    public ResponseEntity<ErrorResponse> handleNoMatchingSupplies() {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(SupplyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(SupplyNotFoundException exception) {
        final var status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), exception.reason.name(), exception.getMessage()));
    }

    @ExceptionHandler(DuplicatedSupplyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicated(DuplicatedSupplyException exception) {
        final var status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), exception.reason.name(), exception.getMessage()));
    }
}
