package br.com.fiap.postech.adapter.input.service.mapper;

import br.com.fiap.postech.adapter.input.api.model.NeededSupplyData;
import br.com.fiap.postech.adapter.input.api.model.PaginatedServiceResponse;
import br.com.fiap.postech.adapter.input.api.model.ServiceData;
import br.com.fiap.postech.adapter.input.api.model.ServiceRequest;
import br.com.fiap.postech.adapter.input.api.model.ServiceStatus;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.domain.service.model.NeededSupply;
import br.com.fiap.postech.domain.service.model.Service;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceMapperTest {

    @Test
    void should_map_from_api_request_to_service() {
        ServiceRequest request = new ServiceRequest()
                .serviceOrderId(10L)
                .catalogServiceId(5L)
                .price(new BigDecimal("150.00"));

        Service result = ServiceMapper.fromApiRequest(request);

        assertThat(result.getCatalogServiceId()).isEqualTo(5L);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void should_map_from_api_data_to_service() {
        ServiceData data = new ServiceData()
                .id(1L)
                .catalogServiceId(5L)
                .price(new BigDecimal("200.00"))
                .status(ServiceStatus.APPROVED);

        Service result = ServiceMapper.fromApiData(data);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCatalogServiceId()).isEqualTo(5L);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        // Status é ignorado em updates normais - alterações só via endpoints de progresso
        assertThat(result.getStatus()).isNull();
    }

    @Test
    void should_map_service_to_api_data() {
        ServiceEntity entity = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("100.00")).status("AWAITING_APPROVAL").build();

        entity.setNeededSupplies(List.of(
                NeededSupply.builder().idSupply(1L).note("urgent").quantity(2).build()
        ));

        ServiceData result = ServiceMapper.toApiData(entity);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getServiceOrderId()).isEqualTo(10L);
        assertThat(result.getCatalogServiceId()).isEqualTo(5L);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getStatus()).isEqualTo(ServiceStatus.AWAITING_APPROVAL);
        assertThat(result.getNeededSupplies()).hasSize(1);
        assertThat(result.getNeededSupplies().get(0).getIdSupply()).isEqualTo(1L);
    }

    @Test
    void should_map_null_status_to_null_in_api_data() {
        ServiceEntity entity = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("100.00")).status(null).build();

        ServiceData result = ServiceMapper.toApiData(entity);

        assertThat(result.getStatus()).isNull();
    }

    @Test
    void should_map_from_scroll_page_to_paginated_response() {
        Service one = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(3L)
                .price(new BigDecimal("50.00")).status("IN_PROGRESS").build();
        Service two = ServiceEntity.builder()
                .id(2L).serviceOrderId(10L).catalogServiceId(4L)
                .price(new BigDecimal("75.00")).status("COMPLETED").build();

        ScrollPage<Service> page = ScrollPage.<Service>builder()
                .data(List.of(one, two)).cursor("2").isLast(false).pageSize(2).build();

        PaginatedServiceResponse response = ServiceMapper.toPaginatedResponse(page);

        assertThat(response.getPageSize()).isEqualTo(2);
        assertThat(response.getCursor()).isEqualTo("2");
        assertThat(response.getIsLast()).isFalse();
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getStatus()).isEqualTo(ServiceStatus.IN_PROGRESS);
        assertThat(response.getData().get(1).getStatus()).isEqualTo(ServiceStatus.COMPLETED);
    }

    @Test
    void should_map_needed_supplies_from_api_data() {
        NeededSupplyData supplyData = new NeededSupplyData()
                .idSupply(2L).note("check stock").quantity(3);

        ServiceData data = new ServiceData()
                .id(1L).catalogServiceId(5L).price(new BigDecimal("100.00"))
                .neededSupplies(List.of(supplyData));

        Service result = ServiceMapper.fromApiData(data);

        assertThat(result.getNeededSupplies()).hasSize(1);
        assertThat(result.getNeededSupplies().get(0).getIdSupply()).isEqualTo(2L);
        assertThat(result.getNeededSupplies().get(0).getQuantity()).isEqualTo(3);
    }

}
