package br.com.fiap.postech.adapter.input.owner.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import br.com.fiap.postech.adapter.input.api.model.OwnerData;
import br.com.fiap.postech.adapter.input.api.model.OwnerRequest;
import br.com.fiap.postech.adapter.input.api.model.PaginatedOwnerResponse;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.model.Owner;

public class OwnerMapperTest {

    @Test
    void should_map_from_scroll_page_to_paginated_response() {
        Owner first = OwnerEntity.builder()
                .id(1L)
                .name("Name-001")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("1231432")
                .email("teste001@email.com")
                .build();
        Owner second = OwnerEntity.builder()
                .id(2L)
                .name("Name-002")
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("1231432")
                .email("teste002@email.com")
                .build();

        ScrollPage<Owner> page = ScrollPage.<Owner>builder()
                .data(List.of(first, second))
                .cursor("2")
                .isLast(false)
                .pageSize(2)
                .build();

        PaginatedOwnerResponse expected = new PaginatedOwnerResponse()
                .pageSize(2)
                .cursor("2")
                .isLast(false)
                .data(List.of(
                        new OwnerData()
                                .id(1L)
                                .name("Name-001")
                                .document("21705047000120")
                                .documentType(DocumentType.CNPJ)
                                .phone("1231432")
                                .email("teste001@email.com"),
                        new OwnerData()
                                .id(2L)
                                .name("Name-002")
                                .document("68478685030")
                                .documentType(DocumentType.CPF)
                                .phone("1231432")
                                .email("teste002@email.com")
                ));

        PaginatedOwnerResponse mapped = OwnerMapper.toPaginatedResponse(page);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    @Test
    void should_map_from_api_request_to_domain_owner() {
        OwnerRequest request = new OwnerRequest()
                .name("Name-003")
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("3333333333")
                .email("teste003@email.com");
        OwnerEntity expected = OwnerEntity.builder()
                .name("Name-003")
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("3333333333")
                .email("teste003@email.com")
                .build();

        Owner mapped = OwnerMapper.fromApiRequest(request);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_api_data_to_domain_owner() {
        OwnerData data = new OwnerData()
                .id(7L)
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("77777777")
                .email("teste007@email.com");
        OwnerEntity expected = OwnerEntity.builder()
                .id(7L)
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("77777777")
                .email("teste007@email.com")
                .build();

        Owner mapped = OwnerMapper.fromApiData(data);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void should_map_from_domain_owner_to_api_data() {
        Owner owner = OwnerEntity.builder()
                .id(11L)
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("11111111111")
                .email("teste011@email.com")
                .build();
        OwnerData expected = new OwnerData()
                .id(11L)
                .document("68478685030")
                .documentType(DocumentType.CPF)
                .phone("11111111111")
                .email("teste011@email.com");

        OwnerData mapped = OwnerMapper.toApiData(owner);

        assertThat(mapped)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

}
