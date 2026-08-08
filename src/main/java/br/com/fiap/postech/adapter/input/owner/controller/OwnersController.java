package br.com.fiap.postech.adapter.input.owner.controller;

import br.com.fiap.postech.adapter.input.api.model.OwnerData;
import br.com.fiap.postech.adapter.input.api.model.OwnerRequest;
import br.com.fiap.postech.adapter.input.api.model.PaginatedOwnerResponse;
import br.com.fiap.postech.adapter.input.owner.mapper.OwnerMapper;
import br.com.fiap.postech.domain.owner.usecase.OwnerUseCase;
import br.com.fiap.postech.port.api.OwnersApi;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OwnersController implements OwnersApi {
    private final OwnerUseCase ownerUseCase;
    
    @Override
    public ResponseEntity<PaginatedOwnerResponse> listOwners(String document, Integer pageSize, String cursor) {
        final var pageResult = ownerUseCase.scroll(document, pageSize, cursor);
        final var responseBody = OwnerMapper.toPaginatedResponse(pageResult);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<OwnerData> getOwnerById(Long id) {
        final var owner = ownerUseCase.getById(id);
        final var responseBody = OwnerMapper.toApiData(owner);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<OwnerData> createOwner(OwnerRequest ownerRequest) {
        final var newOwner = OwnerMapper.fromApiRequest(ownerRequest);
        final var created = ownerUseCase.create(newOwner);
        final var responseBody = OwnerMapper.toApiData(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    public ResponseEntity<OwnerData> updateOwner(Long id, OwnerData ownerData) {
        final var existingOwner = OwnerMapper.fromApiData(ownerData);
        final var updated = ownerUseCase.update(id, existingOwner);
        final var responseBody = OwnerMapper.toApiData(updated);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<Void> deleteOwner(Long id) {
        ownerUseCase.delete(id);

        return ResponseEntity.accepted().build();
    }
}
