package br.com.fiap.postech.adapter.input.catalogservice.mapper;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogServicesMapperTest {
    @Test
    void should_map_from_scroll_page_to_paginated_response() {
        List<NeededSupplyEntity> neededSupplyEntitiesFirst = new ArrayList<>();
        neededSupplyEntitiesFirst.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Tinta")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).
                build());

        CatalogServices first = CatalogServicesEntity.builder()
                .id(1L)
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("890.90"))
                .supplies(neededSupplyEntitiesFirst)
                .build();

        List<NeededSupplyEntity> neededSupplyEntitiesSecond = new ArrayList<>();
        neededSupplyEntitiesSecond.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(2L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(2L)
                        .name("Oleo")
                        .unitPrice(BigDecimal.valueOf(200))
                        .build()).
                build());

        CatalogServices second = CatalogServicesEntity.builder()
                .id(2L)
                .name("Troca de oleo")
                .description("Troca de oleo do motor")
                .basePrice(new BigDecimal("300.90"))
                .supplies(neededSupplyEntitiesSecond)
                .build();

        ScrollPage<CatalogServices> page = ScrollPage.<CatalogServices>builder()
                .data(List.of(first, second))
                .cursor("2")
                .isLast(false)
                .pageSize(2)
                .build();

        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        List<NeededSupplyData> neededSupplyDataTwo = new ArrayList<>();
        neededSupplyDataTwo.add(new NeededSupplyData().idSupply(2L).quantity(100));

        PaginatedCatalogServiceResponse expected = new PaginatedCatalogServiceResponse()
                .pageSize(2)
                .cursor("2")
                .isLast(false)
                .data(List.of(
                        new CatalogServiceData()
                                .id(1L)
                                .name("Pintura")
                                .description("Pintura do veiculo")
                                .basePrice(new BigDecimal("890.90"))
                                .neededSupplies(neededSupplyDataOne),
                        new CatalogServiceData()
                                .id(2L)
                                .name("Troca de oleo")
                                .description("Troca de oleo do motor")
                                .basePrice(new BigDecimal("300.90"))
                                .neededSupplies(neededSupplyDataTwo)
                ));

        PaginatedCatalogServiceResponse mapped = CatalogServicesMapper.toPaginatedResponse(page);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_api_request_to_domain_supply() {
        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        CatalogServiceRequest request = new CatalogServiceRequest()
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("890.9"))
                .neededSupplies(neededSupplyDataOne);

        List<NeededSupplyEntity> neededSupplyEntitiesFirst = new ArrayList<>();
        neededSupplyEntitiesFirst.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Tinta")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).
                build());

        CatalogServicesEntity expected = CatalogServicesEntity.builder()
                .id(1L)
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("890.9"))
                .supplies(neededSupplyEntitiesFirst)
                .build();

        CatalogServices mapped = CatalogServicesMapper.fromApiRequest(request);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("supplies", "id")
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_api_data_to_domain_supply() {
        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        CatalogServiceData data = new CatalogServiceData()
                .id(1L)
                .name("Troca de Pneus")
                .description("Troca dos 4 pneus de um carro")
                .basePrice(new BigDecimal("234.6")).neededSupplies(neededSupplyDataOne);

        List<NeededSupplyEntity> neededSupplyEntitiesFirst = new ArrayList<>();
        neededSupplyEntitiesFirst.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Pneu")
                        .unitPrice(BigDecimal.valueOf(15000))
                        .build()).
                build());

        CatalogServicesEntity expected = CatalogServicesEntity.builder()
                .id(1L)
                .name("Troca de Pneus")
                .description("Troca dos 4 pneus de um carro")
                .basePrice(new BigDecimal("234.6"))
                .supplies(neededSupplyEntitiesFirst)
                .build();

        CatalogServices mapped = CatalogServicesMapper.fromApiData(data);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("supplies")
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_domain_catalog_services_to_api_data() {
        List<NeededSupplyEntity> neededSupplyEntitiesFirst = new ArrayList<>();
        neededSupplyEntitiesFirst.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Tinta")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).
                build());

        CatalogServices catalogServices = CatalogServicesEntity.builder()
                .id(1L)
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("890.90"))
                .supplies(neededSupplyEntitiesFirst)
                .build();

        List<NeededSupplyData> neededSupplyDataOne = new ArrayList<>();
        neededSupplyDataOne.add(new NeededSupplyData().idSupply(1L).quantity(100));

        CatalogServiceData expected = new CatalogServiceData()
                .id(1L)
                .name("Pintura")
                .description("Pintura do veiculo")
                .basePrice(new BigDecimal("890.90")).neededSupplies(neededSupplyDataOne);

        CatalogServiceData mapped = CatalogServicesMapper.toApiData(catalogServices);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
