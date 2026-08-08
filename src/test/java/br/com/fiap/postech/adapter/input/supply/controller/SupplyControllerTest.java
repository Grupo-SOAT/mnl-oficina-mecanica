package br.com.fiap.postech.adapter.input.supply.controller;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.adapter.input.api.model.PaginatedSupplyResponse;
import br.com.fiap.postech.adapter.input.api.model.SupplyData;
import br.com.fiap.postech.adapter.input.api.model.SupplyRequest;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.supply.exception.DuplicatedSupplyException;
import br.com.fiap.postech.domain.supply.exception.SupplyNotFoundException;
import br.com.fiap.postech.domain.supply.model.Supply;
import br.com.fiap.postech.domain.supply.usecase.SupplyUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyControllerTest {
    @Mock
    private SupplyUseCase supplyUseCase;

    @InjectMocks
    private SupplyController controller;

    @Test
    void should_return_ok_with_empty_payload_when_scroll_result_is_empty() {
        ScrollPage<Supply> emptyPage = ScrollPage.<Supply>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        PaginatedSupplyResponse expectedResponseBody = new PaginatedSupplyResponse()
                .pageSize(10)
                .cursor(null)
                .isLast(true);
        when(supplyUseCase.scroll("SKU", 10, "0")).thenReturn(emptyPage);

        ResponseEntity<PaginatedSupplyResponse> response = controller.listSupplies("SKU", 10, "0");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_return_ok_with_paginated_data_when_scroll_has_content() {
        Supply one = SupplyEntity.builder()
                .id(1L).sku("SKU-1").name("A").description("A").unitPrice(new BigDecimal("10.00"))
                .reservedQuantity(1).availableQuantity(9)
                .build();
        Supply two = SupplyEntity.builder()
                .id(2L).sku("SKU-2").name("B").description("B").unitPrice(new BigDecimal("11.00"))
                .reservedQuantity(0).availableQuantity(10)
                .build();

        ScrollPage<Supply> page = ScrollPage.<Supply>builder()
                .data(List.of(one, two))
                .cursor("2")
                .isLast(false)
                .pageSize(2)
                .build();
        PaginatedSupplyResponse expectedResponseBody = new PaginatedSupplyResponse()
                .pageSize(2)
                .cursor("2")
                .isLast(false)
                .data(List.of(
                        new SupplyData()
                                .id(1L)
                                .sku("SKU-1")
                                .name("A")
                                .description("A")
                                .unitPrice(new BigDecimal("10.00"))
                                .suppliedBy(null)
                                .reservedQuantity(1)
                                .availableQuantity(9),
                        new SupplyData()
                                .id(2L)
                                .sku("SKU-2")
                                .name("B")
                                .description("B")
                                .unitPrice(new BigDecimal("11.00"))
                                .suppliedBy(null)
                                .reservedQuantity(0)
                                .availableQuantity(10)
                ));
        when(supplyUseCase.scroll(null, 2, null)).thenReturn(page);

        ResponseEntity<PaginatedSupplyResponse> response = controller.listSupplies(null, 2, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_return_supply_by_id() {
        Supply supply = SupplyEntity.builder()
                .id(7L).sku("SKU-7").name("Nome").description("Desc").unitPrice(new BigDecimal("33.00"))
                .reservedQuantity(2).availableQuantity(8)
                .build();
        SupplyData expectedResponseBody = new SupplyData()
                .id(7L)
                .sku("SKU-7")
                .name("Nome")
                .description("Desc")
                .unitPrice(new BigDecimal("33.00"))
                .suppliedBy(null)
                .reservedQuantity(2)
                .availableQuantity(8);
        when(supplyUseCase.getById(7L)).thenReturn(supply);

        ResponseEntity<SupplyData> response = controller.getSupplyById(7L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_create_supply_and_return_created() {
        SupplyRequest request = new SupplyRequest()
                .sku("SKU-NEW")
                .name("Novo")
                .description("Novo item")
                .unitPrice(new BigDecimal("99.90"))
                .suppliedBy(5L);

        Supply created = SupplyEntity.builder()
                .id(100L)
                .sku("SKU-NEW")
                .name("Novo")
                .description("Novo item")
                .unitPrice(new BigDecimal("99.90"))
                .suppliedBy(5L)
                .reservedQuantity(0)
                .availableQuantity(0)
                .build();
        SupplyData expectedResponseBody = new SupplyData()
                .id(100L)
                .sku("SKU-NEW")
                .name("Novo")
                .description("Novo item")
                .unitPrice(new BigDecimal("99.90"))
                .suppliedBy(5L)
                .reservedQuantity(0)
                .availableQuantity(0);

        when(supplyUseCase.create(any(Supply.class))).thenReturn(created);

        ResponseEntity<SupplyData> response = controller.createSupply(request);

        ArgumentCaptor<Supply> captor = ArgumentCaptor.forClass(Supply.class);
        verify(supplyUseCase).create(captor.capture());

        assertThat(captor.getValue())
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt", "reservedQuantity", "availableQuantity")
                .isEqualTo(SupplyEntity.builder()
                        .sku("SKU-NEW")
                        .name("Novo")
                        .description("Novo item")
                        .unitPrice(new BigDecimal("99.90"))
                        .suppliedBy(5L)
                        .build());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_update_supply_and_return_ok() {
        SupplyData request = new SupplyData()
                .id(77L)
                .sku("SKU-77")
                .name("Atualizado")
                .description("Desc")
                .unitPrice(new BigDecimal("22.00"))
                .reservedQuantity(1)
                .availableQuantity(4);

        Supply updated = SupplyEntity.builder()
                .id(77L)
                .sku("SKU-77")
                .name("Atualizado")
                .description("Desc")
                .unitPrice(new BigDecimal("22.00"))
                .reservedQuantity(1)
                .availableQuantity(4)
                .build();
        SupplyData expectedResponseBody = new SupplyData()
                .id(77L)
                .sku("SKU-77")
                .name("Atualizado")
                .description("Desc")
                .unitPrice(new BigDecimal("22.00"))
                .suppliedBy(null)
                .reservedQuantity(1)
                .availableQuantity(4);

        when(supplyUseCase.update(any(Long.class), any(Supply.class))).thenReturn(updated);

        ResponseEntity<SupplyData> response = controller.updateSupply(77L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_delete_supply_and_return_accepted() {
        ResponseEntity<Void> response = controller.deleteSupply(55L);

        verify(supplyUseCase).delete(55L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void should_handle_no_matching_supplies_with_no_content() {
        ResponseEntity<ErrorResponse> response = controller.handleNoMatchingSupplies();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_handle_not_found_exception() {
        final var exception = new SupplyNotFoundException(-1L);
        final var expectedHttpStatus = HttpStatus.NOT_FOUND;
        final var expectedResponseBody = new ErrorResponse()
                .code(expectedHttpStatus.value())
                .reason(exception.reason.name())
                .message(exception.getMessage());

        ResponseEntity<ErrorResponse> response = controller.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_handle_conflict_exception() {
        final var exception = new DuplicatedSupplyException("123");
        final var expectedHttpStatus = HttpStatus.CONFLICT;
        final var expectedResponseBody = new ErrorResponse()
                .code(expectedHttpStatus.value())
                .reason(exception.reason.name())
                .message(exception.getMessage());

        ResponseEntity<ErrorResponse> response = controller.handleDuplicated(exception);

        assertThat(response.getStatusCode()).isEqualTo(expectedHttpStatus);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }
}
