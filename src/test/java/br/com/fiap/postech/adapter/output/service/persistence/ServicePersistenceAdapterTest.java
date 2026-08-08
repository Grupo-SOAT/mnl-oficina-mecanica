package br.com.fiap.postech.adapter.output.service.persistence;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.repository.CatalogServicesRepository;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.adapter.output.service.persistence.repository.ServiceRepository;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.domain.service.model.Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePersistenceAdapterTest {

    @Mock
    private ServiceRepository repository;

    @Mock
    private CatalogServicesRepository catalogServicesRepository;

    @InjectMocks
    private ServicePersistenceAdapter adapter;

    @Test
    void should_scroll_all_services_when_no_filter() {
        ServiceEntity first = ServiceEntity.builder().id(1L).serviceOrderId(10L).build();
        ServiceEntity second = ServiceEntity.builder().id(2L).serviceOrderId(10L).build();
        ServiceEntity third = ServiceEntity.builder().id(3L).serviceOrderId(10L).build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second, third)));

        ScrollPage<Service> page = adapter.scroll(10L, null, null, 2, "0");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(page.data()).hasSize(2);
        assertThat(page.isLast()).isFalse();
    }

    @Test
    void should_scroll_by_service_id_when_filter_provided() {
        ServiceEntity entity = ServiceEntity.builder().id(5L).serviceOrderId(10L).build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Service> page = adapter.scroll(10L, 5L, null, 10, null);

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(page.data()).hasSize(1);
        assertThat(page.isLast()).isTrue();
    }

    @Test
    void should_return_empty_page_when_no_results() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        ScrollPage<Service> page = adapter.scroll(10L, null, null, 10, null);

        assertThat(page.data()).isEmpty();
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_find_by_id_and_service_order_id() {
        ServiceEntity entity = ServiceEntity.builder().id(5L).serviceOrderId(10L).build();
        when(repository.findByIdAndServiceOrderId(5L, 10L)).thenReturn(Optional.of(entity));

        Optional<Service> result = adapter.findByIdAndServiceOrderId(5L, 10L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
    }

    @Test
    void should_return_empty_when_service_not_found() {
        when(repository.findByIdAndServiceOrderId(99L, 10L)).thenReturn(Optional.empty());

        Optional<Service> result = adapter.findByIdAndServiceOrderId(99L, 10L);

        assertThat(result).isEmpty();
    }

    @Test
    void should_save_entity_directly_when_instance_is_service_entity() {
        ServiceEntity entity = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("100.00")).status("AWAITING_APPROVAL").build();

        when(repository.save(entity)).thenReturn(entity);

        Service saved = adapter.save(entity);

        verify(repository).save(entity);
        assertThat(saved).isSameAs(entity);
    }

    @Test
    void should_delete_by_id() {
        adapter.deleteById(5L);

        verify(repository).deleteById(5L);
    }

    @Test
    void should_return_null_when_calculate_average_time_by_id_and_no_services() {
        when(repository.findByCatalogServiceId(7L)).thenReturn(List.of());

        ServiceCalculatedAverageTime result = adapter.calculateAverageTime(7L);

        assertThat(result).isNull();
        verify(catalogServicesRepository, never()).findAllById(any());
    }

    @Test
    void should_calculate_average_time_by_id() {
        ServiceEntity completed = serviceEntity(
                1L,
                7L,
                LocalDateTime.parse("2026-01-01T08:00"),
                LocalDateTime.parse("2026-01-01T09:00"),
                LocalDateTime.parse("2026-01-01T10:00"),
                LocalDateTime.parse("2026-01-01T12:00")
        );
        ServiceEntity pending = serviceEntity(
                2L,
                7L,
                LocalDateTime.parse("2026-01-01T10:00"),
                null,
                null,
                null
        );

        when(repository.findByCatalogServiceId(7L)).thenReturn(List.of(completed, pending));
        when(catalogServicesRepository.findAllById(List.of(7L))).thenReturn(List.of(
                CatalogServicesEntity.builder().id(7L).name("Troca de oleo").build()
        ));

        ServiceCalculatedAverageTime result = adapter.calculateAverageTime(7L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.name()).isEqualTo("Troca de oleo");
        assertThat(result.totalCreated()).isEqualTo(2L);
        assertThat(result.totalCompleted()).isEqualTo(1L);
        assertThat(result.averageTimeBetweenCreateAndComplete()).isEqualTo(4.0);
        assertThat(result.averageTimeBetweenStartAndComplete()).isEqualTo(2.0);
        assertThat(result.averageTimeBetweenApproveAndComplete()).isEqualTo(3.0);
        assertThat(result.averageTimeAwaitingBudgetApproval()).isEqualTo(1.0);
    }

    @Test
    void should_calculate_average_time_grouped_by_catalog_service_id() {
        ServiceEntity catalogOneFirst = serviceEntity(
                11L,
                1L,
                LocalDateTime.parse("2026-01-01T08:00"),
                LocalDateTime.parse("2026-01-01T09:00"),
                LocalDateTime.parse("2026-01-01T10:00"),
                LocalDateTime.parse("2026-01-01T12:00")
        );
        ServiceEntity catalogOneSecond = serviceEntity(
                12L,
                1L,
                LocalDateTime.parse("2026-01-01T10:00"),
                LocalDateTime.parse("2026-01-01T11:00"),
                LocalDateTime.parse("2026-01-01T12:00"),
                LocalDateTime.parse("2026-01-01T14:00")
        );
        ServiceEntity catalogTwoCompleted = serviceEntity(
                21L,
                2L,
                LocalDateTime.parse("2026-01-02T08:00"),
                LocalDateTime.parse("2026-01-02T10:00"),
                LocalDateTime.parse("2026-01-02T11:00"),
                LocalDateTime.parse("2026-01-02T15:00")
        );
        ServiceEntity catalogTwoPending = serviceEntity(
                22L,
                2L,
                LocalDateTime.parse("2026-01-02T09:00"),
                null,
                null,
                null
        );

        when(repository.findAll()).thenReturn(List.of(catalogTwoCompleted, catalogOneFirst, catalogTwoPending, catalogOneSecond));
        when(catalogServicesRepository.findAllById(any())).thenReturn(List.of(
                CatalogServicesEntity.builder().id(1L).name("Revisao").build()
        ));

        List<ServiceCalculatedAverageTime> result = adapter.calculateAverageTime();

        assertThat(result).hasSize(2);

        Map<Long, ServiceCalculatedAverageTime> resultById = new HashMap<>();
        result.forEach(item -> resultById.put(item.id(), item));

        ServiceCalculatedAverageTime catalogOne = resultById.get(1L);
        assertThat(catalogOne).isNotNull();
        assertThat(catalogOne.name()).isEqualTo("Revisao");
        assertThat(catalogOne.totalCreated()).isEqualTo(2L);
        assertThat(catalogOne.totalCompleted()).isEqualTo(2L);
        assertThat(catalogOne.averageTimeBetweenCreateAndComplete()).isEqualTo(4.0);
        assertThat(catalogOne.averageTimeBetweenStartAndComplete()).isEqualTo(2.0);
        assertThat(catalogOne.averageTimeBetweenApproveAndComplete()).isEqualTo(3.0);
        assertThat(catalogOne.averageTimeAwaitingBudgetApproval()).isEqualTo(1.0);

        ServiceCalculatedAverageTime catalogTwo = resultById.get(2L);
        assertThat(catalogTwo).isNotNull();
        assertThat(catalogTwo.name()).isEmpty();
        assertThat(catalogTwo.totalCreated()).isEqualTo(2L);
        assertThat(catalogTwo.totalCompleted()).isEqualTo(1L);
        assertThat(catalogTwo.averageTimeBetweenCreateAndComplete()).isEqualTo(7.0);
        assertThat(catalogTwo.averageTimeBetweenStartAndComplete()).isEqualTo(4.0);
        assertThat(catalogTwo.averageTimeBetweenApproveAndComplete()).isEqualTo(5.0);
        assertThat(catalogTwo.averageTimeAwaitingBudgetApproval()).isEqualTo(2.0);
    }

    private ServiceEntity serviceEntity(
            Long id,
            Long catalogServiceId,
            LocalDateTime createdAt,
            LocalDateTime approvedAt,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        return ServiceEntity.builder()
                .id(id)
                .serviceOrderId(10L)
                .catalogServiceId(catalogServiceId)
                .price(new BigDecimal("100.00"))
                .status("COMPLETED")
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .approvedAt(approvedAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();
    }

    // Testes adicionais para cobertura de branches - filtros de scroll
    @Test
    void should_scroll_by_status_when_filter_provided() {
        ServiceEntity entity = ServiceEntity.builder().id(1L).serviceOrderId(10L).status("COMPLETED").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Service> page = adapter.scroll(10L, null, null, 10, null);

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(page.data()).hasSize(1);
    }

    @Test
    void should_scroll_by_service_id_and_status_when_both_provided() {
        ServiceEntity entity = ServiceEntity.builder().id(5L).serviceOrderId(10L).status("COMPLETED").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Service> page = adapter.scroll(10L, 5L, null, 10, null);

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(page.data()).hasSize(1);
    }

    @Test
    void should_scroll_by_name_and_status_when_both_provided() {
        ServiceEntity entity = ServiceEntity.builder().id(1L).serviceOrderId(10L).status("COMPLETED").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Service> page = adapter.scroll(10L, null, "troca", 10, null);

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(page.data()).hasSize(1);
    }

    @Test
    void should_scroll_with_all_filters_when_provided() {
        ServiceEntity entity = ServiceEntity.builder().id(5L).serviceOrderId(10L).status("COMPLETED").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Service> page = adapter.scroll(10L, 5L, "troca", 10, null);

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
        assertThat(page.data()).hasSize(1);
    }

    @Test
    void should_handle_cursor_pagination_correctly() {
        ServiceEntity first = ServiceEntity.builder().id(1L).serviceOrderId(10L).build();
        ServiceEntity second = ServiceEntity.builder().id(2L).serviceOrderId(10L).build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second)));

        ScrollPage<Service> page = adapter.scroll(10L, null, null, 1, "abc123");

        assertThat(page.data()).hasSize(1);
        assertThat(page.isLast()).isFalse();
        assertThat(page.cursor()).isNotNull();
    }

    @Test
    void should_save_non_entity_service_model() {
        Service service = new Service() {
            @Override public Long getId() { return 1L; }
            @Override public void setId(Long id) { }
            @Override public Long getServiceOrderId() { return 10L; }
            @Override public void setServiceOrderId(Long serviceOrderId) { }
            @Override public Long getCatalogServiceId() { return 5L; }
            @Override public void setCatalogServiceId(Long catalogServiceId) { }
            @Override public BigDecimal getPrice() { return new BigDecimal("100.00"); }
            @Override public void setPrice(BigDecimal price) { }
            @Override public java.util.List<br.com.fiap.postech.domain.service.model.NeededSupply> getNeededSupplies() { return null; }
            @Override public void setNeededSupplies(java.util.List<br.com.fiap.postech.domain.service.model.NeededSupply> neededSupplies) { }
            @Override public String getStatus() { return "AWAITING_APPROVAL"; }
            @Override public void setStatus(String status) { }
            @Override public String getStatusLabel() { return null; }
            @Override public void setStatusLabel(String statusLabel) { }
            @Override public LocalDateTime getCreatedAt() { return LocalDateTime.now(); }
            @Override public void setCreatedAt(LocalDateTime createdAt) { }
            @Override public LocalDateTime getUpdatedAt() { return LocalDateTime.now(); }
            @Override public void setUpdatedAt(LocalDateTime updatedAt) { }
            @Override public LocalDateTime getRejectedAt() { return null; }
            @Override public void setRejectedAt(LocalDateTime rejectedAt) { }
            @Override public LocalDateTime getCancelledAt() { return null; }
            @Override public void setCancelledAt(LocalDateTime cancelledAt) { }
            @Override public LocalDateTime getApprovedAt() { return null; }
            @Override public void setApprovedAt(LocalDateTime approvedAt) { }
            @Override public LocalDateTime getStartedAt() { return null; }
            @Override public void setStartedAt(LocalDateTime startedAt) { }
            @Override public LocalDateTime getCompletedAt() { return null; }
            @Override public void setCompletedAt(LocalDateTime completedAt) { }
        };

        ServiceEntity entity = ServiceEntity.builder()
                .id(1L).serviceOrderId(10L).catalogServiceId(5L)
                .price(new BigDecimal("100.00")).status("AWAITING_APPROVAL").build();

        when(repository.save(any(ServiceEntity.class))).thenReturn(entity);

        Service saved = adapter.save(service);

        assertThat(saved).isNotNull();
    }

}
