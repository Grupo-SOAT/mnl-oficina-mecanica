package br.com.fiap.postech.adapter.input.supply.mapper;

import br.com.fiap.postech.adapter.input.api.model.PaginatedSupplyResponse;
import br.com.fiap.postech.adapter.input.api.model.SupplyData;
import br.com.fiap.postech.adapter.input.api.model.SupplyRequest;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.supply.model.Supply;
import org.jspecify.annotations.NonNull;

public class SupplyMapper {
    public static Supply fromApiRequest(@NonNull SupplyRequest request) {
        return SupplyEntity.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .unitPrice(request.getUnitPrice())
                .suppliedBy(request.getSuppliedBy())
                .build();
    }

    public static Supply fromApiData(@NonNull SupplyData data) {
        return SupplyEntity.builder()
                .id(data.getId())
                .sku(data.getSku())
                .name(data.getName())
                .description(data.getDescription())
                .unitPrice(data.getUnitPrice())
                .suppliedBy(data.getSuppliedBy())
                .reservedQuantity(data.getReservedQuantity())
                .availableQuantity(data.getAvailableQuantity())
                .build();
    }

    public static SupplyData toApiData(@NonNull Supply supply) {
        return new SupplyData()
                .id(supply.getId())
                .sku(supply.getSku())
                .name(supply.getName())
                .description(supply.getDescription())
                .unitPrice(supply.getUnitPrice())
                .suppliedBy(supply.getSuppliedBy())
                .reservedQuantity(supply.getReservedQuantity())
                .availableQuantity(supply.getAvailableQuantity());
    }

    public static PaginatedSupplyResponse toPaginatedResponse(@NonNull ScrollPage<Supply> page) {
        final var result = new PaginatedSupplyResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(SupplyMapper.toApiData(item)));

        return result;
    }
}
