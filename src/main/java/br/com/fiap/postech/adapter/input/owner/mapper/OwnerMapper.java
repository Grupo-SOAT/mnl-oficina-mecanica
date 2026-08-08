package br.com.fiap.postech.adapter.input.owner.mapper;

import br.com.fiap.postech.adapter.input.api.model.OwnerData;
import br.com.fiap.postech.adapter.input.api.model.OwnerRequest;
import br.com.fiap.postech.adapter.input.api.model.PaginatedOwnerResponse;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.model.Owner;

import org.jspecify.annotations.NonNull;

public class OwnerMapper {

    public static Owner fromApiRequest(@NonNull OwnerRequest request) {
        return OwnerEntity.builder()
                .name(request.getName())
                .document(request.getDocument())
                .documentType(request.getDocumentType())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();
    }

    public static Owner fromApiData(@NonNull OwnerData data) {
        return OwnerEntity.builder()
                .id(data.getId())
                .name(data.getName())
                .document(data.getDocument())
                .documentType(data.getDocumentType())
                .phone(data.getPhone())
                .email(data.getEmail())
                .build();
    }

    public static OwnerData toApiData(@NonNull Owner owner) {
        return new OwnerData()
                .id(owner.getId())
                .name(owner.getName())
                .document(owner.getDocument())
                .documentType(owner.getDocumentType())
                .phone(owner.getPhone())
                .email(owner.getEmail());
    }

    public static PaginatedOwnerResponse toPaginatedResponse(@NonNull ScrollPage<Owner> page) {
        final var result = new PaginatedOwnerResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(OwnerMapper.toApiData(item)));

        return result;
    }
}
