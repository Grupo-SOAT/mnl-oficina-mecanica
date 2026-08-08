package br.com.fiap.postech.adapter.input.owner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import br.com.fiap.postech.adapter.input.api.model.OwnerData;
import br.com.fiap.postech.adapter.input.api.model.OwnerRequest;
import br.com.fiap.postech.adapter.input.api.model.PaginatedOwnerResponse;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.domain.owner.usecase.OwnerUseCase;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OwnersControllerTest {
    @Mock
    private OwnerUseCase ownerUseCase;

    @InjectMocks
    private OwnersController controller;

    @Test
    void should_return_ok_with_empty_payload_when_scroll_result_is_empty() {
        ScrollPage<Owner> emptyPage = ScrollPage.<Owner>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        PaginatedOwnerResponse expectedResponseBody = new PaginatedOwnerResponse()
                .pageSize(10)
                .cursor(null)
                .isLast(true);
        when(ownerUseCase.scroll("teste@email.com", 10, "0")).thenReturn(emptyPage);

        ResponseEntity<PaginatedOwnerResponse> response = controller.listOwners("teste@email.com", 10, "0");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }
    
    @Test
    void should_return_ok_with_paginated_data_when_scroll_has_content() {
        Owner one = OwnerEntity.builder()
                .id(1L).email("teste@email.com").name("A").document("31058167049").documentType(DocumentType.CPF)
                .phone("123")
                .build();
        Owner two = OwnerEntity.builder()
                .id(2L).email("teste2@email.com").name("B").document("78209181000158").documentType(DocumentType.CNPJ)
                .phone("456")
                .build();

        ScrollPage<Owner> page = ScrollPage.<Owner>builder()
                .data(List.of(one, two))
                .cursor("2")
                .isLast(false)
                .pageSize(2)
                .build();
        PaginatedOwnerResponse expectedResponseBody = new PaginatedOwnerResponse()
                .pageSize(2)
                .cursor("2")
                .isLast(false)
                .data(List.of(
                        new OwnerData()
                                .id(1L)
                                .email("teste@email.com")
                                .name("A")
                                .document("31058167049")
                                .documentType(DocumentType.CPF)
                                .phone("123"),
                        new OwnerData()
                                .id(2L)
                                .email("teste2@email.com")
                                .name("B")
                                .document("78209181000158")
                                .documentType(DocumentType.CNPJ)
                                .phone("456")
                ));
        when(ownerUseCase.scroll(null, 2, null)).thenReturn(page);

        ResponseEntity<PaginatedOwnerResponse> response = controller.listOwners(null, 2, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_return_owner_by_id() {
        Owner owner = OwnerEntity.builder()
                .id(3L).email("teste3@email.com").name("C").document("31058167049").documentType(DocumentType.CPF)
                .phone("123")
                .build();
        OwnerData expectedResponseBody = new OwnerData()
                .id(3L)
                .email("teste3@email.com")
                .name("C")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("123");
        when(ownerUseCase.getById(3L)).thenReturn(owner);

        ResponseEntity<OwnerData> response = controller.getOwnerById(3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_create_owner_with_documentType_cpf_and_return_created() {
        OwnerRequest request = new OwnerRequest()
                .name("Name")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("1231432")
                .email("teste@email.com");

        Owner created = OwnerEntity.builder()
                .id(100L)
                .name("Name")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("1231432")
                .email("teste@email.com")
                .build();
        OwnerData expectedResponseBody = new OwnerData()
                .id(100L)
                .name("Name")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("1231432")
                .email("teste@email.com");

        when(ownerUseCase.create(any(Owner.class))).thenReturn(created);

        ResponseEntity<OwnerData> response = controller.createOwner(request);

        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerUseCase).create(captor.capture());

        assertThat(captor.getValue())
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(OwnerEntity.builder()
                        .name("Name")
                        .document("31058167049")
                        .documentType(DocumentType.CPF)
                        .phone("1231432")
                        .email("teste@email.com")
                        .build());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_create_owner_with_documentType_cnpj_and_return_created() {
        OwnerRequest request = new OwnerRequest()
                .name("Teste-101")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("1231432")
                .email("teste101@email.com");

        Owner created = OwnerEntity.builder()
                .id(101L)
                .name("Teste-101")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("1231432")
                .email("teste101@email.com")
                .build();
        OwnerData expectedResponseBody = new OwnerData()
                .id(101L)
                .name("Teste-101")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("1231432")
                .email("teste101@email.com");

        when(ownerUseCase.create(any(Owner.class))).thenReturn(created);

        ResponseEntity<OwnerData> response = controller.createOwner(request);

        ArgumentCaptor<Owner> captor = ArgumentCaptor.forClass(Owner.class);
        verify(ownerUseCase).create(captor.capture());

        assertThat(captor.getValue())
                .isNotNull()
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(OwnerEntity.builder()
                        .name("Teste-101")
                        .document("21705047000120")
                        .documentType(DocumentType.CNPJ)
                        .phone("1231432")
                        .email("teste101@email.com")
                        .build());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_update_owner_with_documentType_cpf_and_return_ok() {
        OwnerData request = new OwnerData()
                .id(77L)
                .name("Name-77")
                .email("teste77@email.com")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("123");

        Owner updated = OwnerEntity.builder()
                .id(77L)
                .name("Name-77")
                .email("teste77@email.com")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("123")
                .build();
        OwnerData expectedResponseBody = new OwnerData()
                .id(77L)
                .name("Name-77")
                .email("teste77@email.com")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("123");

        when(ownerUseCase.update(any(Long.class), any(Owner.class))).thenReturn(updated);

        ResponseEntity<OwnerData> response = controller.updateOwner(77L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_update_owner_with_documentType_cnpj_and_return_ok() {
        OwnerData request = new OwnerData()
                .id(78L)
                .name("Name-78")
                .email("teste78@email.com")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("123");

        Owner updated = OwnerEntity.builder()
                .id(78L)
                .name("Name-78")
                .email("teste78@email.com")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("123")
                .build();
        OwnerData expectedResponseBody = new OwnerData()
                .id(78L)
                .name("Name-78")
                .email("teste78@email.com")
                .document("21705047000120")
                .documentType(DocumentType.CNPJ)
                .phone("123");

        when(ownerUseCase.update(any(Long.class), any(Owner.class))).thenReturn(updated);

        ResponseEntity<OwnerData> response = controller.updateOwner(78L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expectedResponseBody);
    }

    @Test
    void should_delete_owner_and_return_accepted() {
        ResponseEntity<Void> response = controller.deleteOwner(55L);

        verify(ownerUseCase).delete(55L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
