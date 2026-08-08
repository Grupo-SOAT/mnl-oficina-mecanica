package br.com.fiap.postech.adapter.input.service.controller;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.adapter.input.api.model.PaginatedServiceResponse;
import br.com.fiap.postech.adapter.input.api.model.ServiceData;
import br.com.fiap.postech.adapter.input.api.model.ServiceRequest;
import br.com.fiap.postech.adapter.input.api.model.ServiceStatus;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.domain.service.usecase.ServiceUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicesControllerTest {

    @Mock
    private ServiceUseCase serviceUseCase;

    @InjectMocks
    private ServicesController controller;

    @Test
    void should_return_ok_with_paginated_data_when_list_has_content() {
        Service one = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("100.00")).status("AWAITING_APPROVAL")
                .statusLabel("Aguardando Aprovação").build();
        ScrollPage<Service> page = ScrollPage.<Service>builder()
                .data(List.of(one)).cursor("1").isLast(true).pageSize(10).build();

        when(serviceUseCase.scroll(10L, null, null, 10, null)).thenReturn(page);

        ResponseEntity<PaginatedServiceResponse> response = controller.listServices(10L, null, null, 10, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(1);
        assertThat(response.getBody().getData().get(0).getId()).isEqualTo(1L);
        assertThat(response.getBody().getData().get(0).getStatusLabel()).isEqualTo("Aguardando Aprovação");
    }

    @Test
    void should_return_no_content_when_no_matching_services() {
        ResponseEntity<Void> response = controller.handleNoMatchingServices();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void should_return_201_when_service_created() {
        ServiceRequest request = new ServiceRequest()
                .serviceOrderId(10L)
                .catalogServiceId(5L)
                .price(new BigDecimal("150.00"));

        ServiceEntity created = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("150.00")).status("AWAITING_APPROVAL")
                .statusLabel("Aguardando Aprovação").build();

        when(serviceUseCase.create(eq(10L), any(Service.class))).thenReturn(created);

        ResponseEntity<ServiceData> response = controller.includeService(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getStatus()).isEqualTo(ServiceStatus.AWAITING_APPROVAL);
        assertThat(response.getBody().getStatusLabel()).isEqualTo("Aguardando Aprovação");
    }

    @Test
    void should_return_ok_when_service_found_by_id() {
        ServiceEntity entity = ServiceEntity.builder()
                .id(5L).serviceOrderId(10L).catalogServiceId(3L)
                .price(new BigDecimal("80.00")).status("IN_PROGRESS")
                .statusLabel("Em Andamento").build();

        when(serviceUseCase.getById(10L, 5L)).thenReturn(entity);

        ResponseEntity<ServiceData> response = controller.getServiceById(10L, 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(5L);
        assertThat(response.getBody().getStatusLabel()).isEqualTo("Em Andamento");
    }

    @Test
    void should_return_404_when_service_not_found() {
        ServiceNotFoundException exception = new ServiceNotFoundException(99L);

        ResponseEntity<ErrorResponse> response = controller.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReason()).isEqualTo("SERVICE_NOT_FOUND");
    }

    @Test
    void should_return_404_when_catalog_service_not_found() {
        CatalogServiceNotFoundException exception = new CatalogServiceNotFoundException(99L);

        ResponseEntity<ErrorResponse> response = controller.handleCatalogServiceNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getReason()).isEqualTo("CATALOG_SERVICE_NOT_FOUND");
    }

    @Test
    void should_return_ok_when_service_updated() {
        ServiceData requestData = new ServiceData()
                .id(5L)
                .catalogServiceId(3L)
                .price(new BigDecimal("90.00"))
                .status(ServiceStatus.AWAITING_APPROVAL);

        ServiceEntity updated = ServiceEntity.builder()
                .id(5L).serviceOrderId(10L).catalogServiceId(3L)
                .price(new BigDecimal("90.00")).status("AWAITING_APPROVAL")
                .statusLabel("Aguardando Aprovação").build();

        when(serviceUseCase.update(eq(10L), eq(5L), any(Service.class))).thenReturn(updated);

        ResponseEntity<ServiceData> response = controller.updateService(10L, 5L, requestData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(ServiceStatus.AWAITING_APPROVAL);
        assertThat(response.getBody().getStatusLabel()).isEqualTo("Aguardando Aprovação");
    }

    @Test
    void should_return_accepted_when_service_deleted() {
        ResponseEntity<Void> response = controller.deleteService(10L, 5L);

        verify(serviceUseCase).delete(10L, 5L);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
