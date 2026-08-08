package br.com.fiap.postech.adapter.input.supply.mapper;

import br.com.fiap.postech.adapter.input.api.model.PaginatedSupplyResponse;
import br.com.fiap.postech.adapter.input.api.model.SupplyData;
import br.com.fiap.postech.adapter.input.api.model.SupplyRequest;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.supply.model.Supply;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SupplyMapperTest {
    @Test
    void should_map_from_scroll_page_to_paginated_response() {
        Supply first = SupplyEntity.builder()
                .id(1L)
                .sku("SKU-001")
                .name("Oleo")
                .description("Oleo sintetico")
                .unitPrice(new BigDecimal("49.90"))
                .suppliedBy(10L)
                .reservedQuantity(2)
                .availableQuantity(8)
                .build();
        Supply second = SupplyEntity.builder()
                .id(2L)
                .sku("SKU-002")
                .name("Filtro")
                .description("Filtro de oleo")
                .unitPrice(new BigDecimal("25.50"))
                .suppliedBy(10L)
                .reservedQuantity(1)
                .availableQuantity(20)
                .build();

        ScrollPage<Supply> page = ScrollPage.<Supply>builder()
                .data(List.of(first, second))
                .cursor("2")
                .isLast(false)
                .pageSize(2)
                .build();

        PaginatedSupplyResponse expected = new PaginatedSupplyResponse()
                .pageSize(2)
                .cursor("2")
                .isLast(false)
                .data(List.of(
                        new SupplyData()
                                .id(1L)
                                .sku("SKU-001")
                                .name("Oleo")
                                .description("Oleo sintetico")
                                .unitPrice(new BigDecimal("49.90"))
                                .suppliedBy(10L)
                                .reservedQuantity(2)
                                .availableQuantity(8),
                        new SupplyData()
                                .id(2L)
                                .sku("SKU-002")
                                .name("Filtro")
                                .description("Filtro de oleo")
                                .unitPrice(new BigDecimal("25.50"))
                                .suppliedBy(10L)
                                .reservedQuantity(1)
                                .availableQuantity(20)
                ));

        PaginatedSupplyResponse mapped = SupplyMapper.toPaginatedResponse(page);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_api_request_to_domain_supply() {
        SupplyRequest request = new SupplyRequest()
                .sku("SKU-001")
                .name("Filtro de oleo")
                .description("Filtro premium")
                .unitPrice(new BigDecimal("49.90"))
                .suppliedBy(99L);
        SupplyEntity expected = SupplyEntity.builder()
                .sku("SKU-001")
                .name("Filtro de oleo")
                .description("Filtro premium")
                .unitPrice(new BigDecimal("49.90"))
                .suppliedBy(99L)
                .build();

        Supply mapped = SupplyMapper.fromApiRequest(request);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_api_data_to_domain_supply() {
        SupplyData data = new SupplyData()
                .id(7L)
                .sku("SKU-007")
                .name("Vela")
                .description("Vela de ignicao")
                .unitPrice(new BigDecimal("15.00"))
                .suppliedBy(5L)
                .reservedQuantity(2)
                .availableQuantity(10);
        SupplyEntity expected = SupplyEntity.builder()
                .id(7L)
                .sku("SKU-007")
                .name("Vela")
                .description("Vela de ignicao")
                .unitPrice(new BigDecimal("15.00"))
                .suppliedBy(5L)
                .reservedQuantity(2)
                .availableQuantity(10)
                .build();

        Supply mapped = SupplyMapper.fromApiData(data);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_domain_supply_to_api_data() {
        Supply supply = SupplyEntity.builder()
                .id(11L)
                .sku("SKU-011")
                .name("Correia")
                .description("Correia dentada")
                .unitPrice(new BigDecimal("199.99"))
                .suppliedBy(3L)
                .reservedQuantity(1)
                .availableQuantity(9)
                .build();
        SupplyData expected = new SupplyData()
                .id(11L)
                .sku("SKU-011")
                .name("Correia")
                .description("Correia dentada")
                .unitPrice(new BigDecimal("199.99"))
                .suppliedBy(3L)
                .reservedQuantity(1)
                .availableQuantity(9);

        SupplyData mapped = SupplyMapper.toApiData(supply);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
