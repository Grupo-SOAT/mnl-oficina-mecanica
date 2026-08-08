package br.com.fiap.postech.adapter.output.serviceorder.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.repository.ServiceOrderRepository;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;

@ExtendWith(MockitoExtension.class)
class ServiceOrderPersistenceAdapterTest {

    @Mock
    private ServiceOrderRepository repository;

    @InjectMocks
    private ServiceOrderPersistenceAdapter adapter;

    @Test
    void should_find_service_order_by_id() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(1L).status("PENDING").build();

        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<ServiceOrder> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void should_return_empty_when_service_order_not_found_by_id() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Optional<ServiceOrder> result = adapter.findById(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void should_save_service_order_entity_directly() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(1L).status("PENDING").build();

        when(repository.save(entity)).thenReturn(entity);

        ServiceOrder result = adapter.save(entity);

        assertThat(result).isSameAs(entity);
    }

    @Test
    void should_delete_service_order_by_id() {
        adapter.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void should_check_if_service_order_exists_by_id() {
        when(repository.existsById(1L)).thenReturn(true);

        assertThat(adapter.existsById(1L)).isTrue();
    }

    @Test
    void should_scroll_default_listing_when_status_is_null() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(5L).status("IN_PROGRESS").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<ServiceOrder> result = adapter.scroll(null, null, null, 10, null);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getStatus()).isEqualTo("IN_PROGRESS");
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void should_scroll_default_listing_when_status_is_blank() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(5L).status("IN_PROGRESS").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<ServiceOrder> result = adapter.scroll("   ", null, null, 10, null);

        assertThat(result.data()).hasSize(1);
    }

    @Test
    void should_scroll_by_specific_status_when_status_provided() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(1L).status("PENDING").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<ServiceOrder> result = adapter.scroll("PENDING", null, null, 10, null);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void should_scroll_by_client_id_and_vehicle_id_filters() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(1L).clientId(10L).vehicleId(20L).status("PENDING").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<ServiceOrder> result = adapter.scroll(null, 10L, 20L, 10, null);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getClientId()).isEqualTo(10L);
        assertThat(result.data().get(0).getVehicleId()).isEqualTo(20L);
    }
}
