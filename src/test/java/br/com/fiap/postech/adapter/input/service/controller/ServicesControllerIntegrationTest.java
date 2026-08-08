package br.com.fiap.postech.adapter.input.service.controller;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.domain.service.exception.NoMatchingServicesException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.domain.service.usecase.ServiceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@WithMockUser(roles = {"MECHANIC"})
@org.springframework.test.context.ContextConfiguration(initializers = br.com.fiap.postech.config.PostgresContainerInitializer.class)
class ServicesControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ServiceUseCase serviceUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void should_return_200_with_paginated_data_when_listing_services() throws Exception {
        Service service = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("100.00")).status("AWAITING_APPROVAL").build();
        ScrollPage<Service> page = ScrollPage.<Service>builder()
                .data(List.of(service)).cursor("1").isLast(true).pageSize(10).build();

        when(serviceUseCase.scroll(10L, null, null, 10, null)).thenReturn(page);

        mockMvc.perform(get("/service-orders/10/services")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.isLast").value(true))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void should_return_204_when_no_services_match() throws Exception {
        when(serviceUseCase.scroll(eq(10L), any(), any(), any(), any()))
                .thenThrow(new NoMatchingServicesException(10L));

        mockMvc.perform(get("/service-orders/10/services"))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return_201_when_creating_service() throws Exception {
        ServiceEntity created = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("150.00")).status("AWAITING_APPROVAL").build();

        when(serviceUseCase.create(eq(10L), any(Service.class))).thenReturn(created);

        mockMvc.perform(post("/service-orders/10/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "serviceOrderId": 10,
                                  "catalogServiceId": 5,
                                  "price": 150.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.price").value(150.00));
    }

    @Test
    void should_return_200_when_getting_service_by_id() throws Exception {
        ServiceEntity entity = ServiceEntity.builder()
                .id(5L).serviceOrderId(10L).catalogServiceId(3L)
                .price(new BigDecimal("80.00")).status("IN_PROGRESS").build();

        when(serviceUseCase.getById(10L, 5L)).thenReturn(entity);

        mockMvc.perform(get("/service-orders/10/services/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.price").value(80.00));
    }

    @Test
    void should_return_404_when_service_not_found() throws Exception {
        when(serviceUseCase.getById(10L, 99L))
                .thenThrow(new ServiceNotFoundException(99L));

        mockMvc.perform(get("/service-orders/10/services/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Service not found for id: 99"));
    }

    @Test
    void should_return_200_when_updating_service() throws Exception {
        ServiceEntity updated = ServiceEntity.builder()
                .id(5L).serviceOrderId(10L).catalogServiceId(3L)
                .price(new BigDecimal("90.00")).status("APPROVED").build();

        when(serviceUseCase.update(eq(10L), eq(5L), any(Service.class))).thenReturn(updated);

        mockMvc.perform(patch("/service-orders/10/services/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 5,
                                  "serviceOrderId": 10,
                                  "catalogServiceId": 3,
                                  "price": 90.00,
                                  "status": "APPROVED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.price").value(90.00));
    }

    @Test
    void should_return_202_when_deleting_service() throws Exception {
        mockMvc.perform(delete("/service-orders/10/services/5")
                        .with(csrf()))
                .andExpect(status().isAccepted());

        verify(serviceUseCase).delete(10L, 5L);
    }

    @Test
    @WithMockUser(roles = {})
    void should_return_403_when_user_has_no_roles() throws Exception {
        mockMvc.perform(get("/service-orders/10/services"))
                .andExpect(status().isForbidden());
    }
}
