package br.com.fiap.postech.adapter.input.catalogservice.controller;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.catalogservices.exception.DuplicatedCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.exception.NoMatchingCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import br.com.fiap.postech.domain.catalogservices.usecase.CatalogServicesUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CatalogServicesControllerTest {
    @Mock
    private CatalogServicesUseCase catalogServicesUseCase;

    @InjectMocks
    private CatalogServicesController controller;

    @Test
    void should_return_ok_with_empty_payload_when_scroll_result_is_empty() {
        ScrollPage<CatalogServices> emptyPage = ScrollPage.<CatalogServices>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        PaginatedCatalogServiceResponse expectedResponseBody = new PaginatedCatalogServiceResponse()
                .pageSize(10)
                .cursor(null)
                .isLast(true);
        when(catalogServicesUseCase.scroll(null, "Pneu", 10, "0")).thenReturn(emptyPage);

        ResponseEntity<PaginatedCatalogServiceResponse> response = controller.listCatalogServices(null, "Pneu", 10, "0");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_return_ok_with_paginated_data_when_scroll_has_content() {
        List<NeededSupplyEntity> neededSupplyEntitiesOne = new ArrayList<>();
        neededSupplyEntitiesOne.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Pneu")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).
                build());

        CatalogServices one = CatalogServicesEntity.builder()
                .id(1L).name("Troca de Pneus").description("Troca dos 4 pneus de um carro")
                .basePrice(BigDecimal.valueOf(234.6)).supplies(neededSupplyEntitiesOne)
                .build();

        List<NeededSupplyEntity> neededSupplyEntitiesTwo = new ArrayList<>();
        neededSupplyEntitiesTwo.add(NeededSupplyEntity.builder().servicesSuppliesId(2L).supplyAmount(10).supply(SupplyEntity.builder().id(2L).name("Roda").build()).build());

        CatalogServices two = CatalogServicesEntity.builder()
                .id(2L).name("Troca de rodas").description("Troca das 4 rodas de um carro")
                .basePrice(BigDecimal.valueOf(5000.6)).supplies(neededSupplyEntitiesTwo)
                .build();

        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        List<NeededSupplyData> neededSupplyDataTwo = new ArrayList<>();
        neededSupplyDataTwo.add(new NeededSupplyData().idSupply(2L).quantity(10));

        ScrollPage<CatalogServices> page = ScrollPage.<CatalogServices>builder()
                .data(List.of(one, two))
                .cursor("2")
                .isLast(false)
                .pageSize(2)
                .build();
        PaginatedCatalogServiceResponse expectedResponseBody = new PaginatedCatalogServiceResponse()
                .pageSize(2)
                .cursor("2")
                .isLast(false)
                .data(List.of(
                        new CatalogServiceData()
                                .id(1L)
                                .name("Troca de Pneus")
                                .description("Troca dos 4 pneus de um carro")
                                .basePrice(BigDecimal.valueOf(234.6))
                                .neededSupplies(neededSupplyDataOne),
                        new CatalogServiceData()
                                .id(2L)
                                .name("Troca de rodas")
                                .description("Troca das 4 rodas de um carro")
                                .basePrice(BigDecimal.valueOf(5000.6))
                                .neededSupplies(neededSupplyDataTwo)
                ));
        when(catalogServicesUseCase.scroll(null, null, 2, null)).thenReturn(page);

        ResponseEntity<PaginatedCatalogServiceResponse> response = controller.listCatalogServices(null, null, 2, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_return_catalog_services_by_id() {
        List<NeededSupplyEntity> neededSupplyEntitiesOne = new ArrayList<>();
        neededSupplyEntitiesOne.add(NeededSupplyEntity.builder().servicesSuppliesId(1L).supplyAmount(100).supply(SupplyEntity.builder().id(1L).name("Pneu").unitPrice(BigDecimal.valueOf(1080)).build()).build());

        CatalogServices catalogServices = CatalogServicesEntity.builder()
                .id(1L).name("Troca de Pneus").description("Troca dos 4 pneus de um carro")
                .basePrice(BigDecimal.valueOf(234.6)).supplies(neededSupplyEntitiesOne)
                .build();

        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        CatalogServiceData expectedResponseBody = new CatalogServiceData()
                .id(1L)
                .name("Troca de Pneus")
                .description("Troca dos 4 pneus de um carro")
                .basePrice(new BigDecimal("234.6")).neededSupplies(neededSupplyDataOne);

        when(catalogServicesUseCase.getById(1L)).thenReturn(catalogServices);

        ResponseEntity<CatalogServiceData> response = controller.getCatalogServiceById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_create_catalog_services_and_return_created() {
        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        CatalogServiceRequest request = new CatalogServiceRequest()
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("250.9"))
                .neededSupplies(neededSupplyDataOne);

        List<NeededSupplyEntity> neededSupplyEntitiesOne = new ArrayList<>();
        neededSupplyEntitiesOne.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Pneu")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).build());

        CatalogServices created = CatalogServicesEntity.builder()
                .id(1L).name("Pintura").description("Pintura do veiculo")
                .basePrice(BigDecimal.valueOf(250.90)).supplies(neededSupplyEntitiesOne)
                .build();

        CatalogServiceData expectedResponseBody = new CatalogServiceData()
                .id(1L)
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("250.9"))
                .neededSupplies(neededSupplyDataOne);

        when(catalogServicesUseCase.create(any(CatalogServices.class))).thenReturn(created);

        ResponseEntity<CatalogServiceData> response = controller.createCatalogService(request);

        ArgumentCaptor<CatalogServices> captor = ArgumentCaptor.forClass(CatalogServices.class);
        verify(catalogServicesUseCase).create(captor.capture());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_update_catalog_services_and_return_ok() {
        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        CatalogServiceData request = new CatalogServiceData()
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("250.9"))
                .neededSupplies(neededSupplyDataOne);

        List<NeededSupplyEntity> neededSupplyEntitiesOne = new ArrayList<>();
        neededSupplyEntitiesOne.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Pneu")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).build());

        CatalogServices updated = CatalogServicesEntity.builder()
                .id(1L).name("Pintura").description("Pintura do veiculo")
                .basePrice(BigDecimal.valueOf(250.90)).supplies(neededSupplyEntitiesOne)
                .build();

        CatalogServiceData expectedResponseBody = new CatalogServiceData()
                .id(1L)
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("250.9"))
                .neededSupplies(neededSupplyDataOne);

        when(catalogServicesUseCase.update(any(Long.class), any(CatalogServices.class))).thenReturn(updated);

        ResponseEntity<CatalogServiceData> response = controller.updateCatalogService(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_delete_catalog_services_and_return_accepted() {
        ResponseEntity<Void> response = controller.deleteCatalogService(90L);

        verify(catalogServicesUseCase).delete(90L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }


    @Test
    void should_handle_no_matching_supplies_with_no_content() {
        ResponseEntity<Void> response = controller.handleNoMatchingCatalogServices(new NoMatchingCatalogServiceException("name"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_handle_not_found_exception() {
        final var exception = new CatalogServiceNotFoundException(-1L);
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
        final var exception = new DuplicatedCatalogServiceException("123");
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
