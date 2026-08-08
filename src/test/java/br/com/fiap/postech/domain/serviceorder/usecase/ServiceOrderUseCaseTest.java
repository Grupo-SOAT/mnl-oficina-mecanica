package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.domain.serviceorder.exception.NoMatchingServiceOrdersException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderClientNotFoundException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderVehicleNotFoundException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ServiceOrderUseCaseTest {

    @Mock
    private ServiceOrderPersistencePort persistencePort;

    @Mock
    private OwnerPersistencePort ownerPersistencePort;

    @Mock
    private VehiclePersistencePort vehiclePersistencePort;

    @Mock
    private ServiceOrderStatusLabelPort statusLabelPort;

    @InjectMocks
    private ServiceOrderUseCase useCase;

    @Test
    void should_delegate_scroll_to_persistence() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).build()))
                .isLast(false)
                .cursor("1")
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, null, null, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll(null, null, null, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_no_matching_when_scroll_result_is_empty() {
        ScrollPage<ServiceOrder> empty = ScrollPage.<ServiceOrder>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, null, null, 10, null)).thenReturn(empty);

        assertThatThrownBy(() -> useCase.scroll(null, null, null, 10, null))
                .isInstanceOf(NoMatchingServiceOrdersException.class)
                .hasMessage("No matching service orders for filter: all");
    }

    @Test
    void should_return_service_order_when_found_by_id() {
        ServiceOrderEntity entity = ServiceOrderEntity.builder().id(5L).build();
        when(persistencePort.findById(5L)).thenReturn(Optional.of(entity));

        ServiceOrder found = useCase.getById(5L);

        assertThat(found).isSameAs(entity);
    }

    @Test
    void should_throw_when_service_order_not_found_by_id() {
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(99L))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessage("Service order not found for id: 99");
    }

    @Test
    void should_create_service_order_with_pending_status() {
        ServiceOrderEntity input = ServiceOrderEntity.builder()
                .clientId(1L)
                .vehicleId(1L)
                .description("Troca de óleo")
                .build();

        when(ownerPersistencePort.findById(1L)).thenReturn(Optional.of(mock(Owner.class)));
        when(vehiclePersistencePort.findById(1L)).thenReturn(Optional.of(mock(Vehicle.class)));
        when(persistencePort.save(any(ServiceOrder.class))).thenAnswer(inv -> inv.getArgument(0));
        when(statusLabelPort.resolve("PENDING")).thenReturn("Recebida");

        ServiceOrder saved = useCase.create(input);

        assertThat(saved.getStatus()).isEqualTo("PENDING");
        assertThat(saved.getStatusLabel()).isEqualTo("Recebida");
    }

    @Test
    void should_throw_when_creating_service_order_with_invalid_client() {
        ServiceOrderEntity input = ServiceOrderEntity.builder()
                .clientId(99L)
                .vehicleId(1L)
                .description("Troca de óleo")
                .build();

        when(ownerPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(ServiceOrderClientNotFoundException.class)
                .hasMessage("Client not found for id: 99");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_creating_service_order_with_invalid_vehicle() {
        ServiceOrderEntity input = ServiceOrderEntity.builder()
                .clientId(1L)
                .vehicleId(99L)
                .description("Troca de óleo")
                .build();

        when(ownerPersistencePort.findById(1L)).thenReturn(Optional.of(mock(Owner.class)));
        when(vehiclePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.create(input))
                .isInstanceOf(ServiceOrderVehicleNotFoundException.class)
                .hasMessage("Vehicle not found for id: 99");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_update_service_order_preserving_id() {
        ServiceOrderEntity existing = ServiceOrderEntity.builder()
                .id(5L)
                .clientId(1L)
                .vehicleId(1L)
                .build();
        ServiceOrderEntity incoming = ServiceOrderEntity.builder()
                .clientId(1L)
                .vehicleId(1L)
                .description("Updated description")
                .estimatedAmount(new BigDecimal("200.00"))
                .build();

        when(ownerPersistencePort.findById(1L)).thenReturn(Optional.of(mock(Owner.class)));
        when(vehiclePersistencePort.findById(1L)).thenReturn(Optional.of(mock(Vehicle.class)));
        when(persistencePort.findById(5L)).thenReturn(Optional.of(existing));
        when(persistencePort.save(any(ServiceOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        ServiceOrder updated = useCase.update(5L, incoming);

        assertThat(updated.getId()).isEqualTo(5L);
    }

    @Test
    void should_throw_when_updating_non_existing_service_order() {
        ServiceOrderEntity incoming = ServiceOrderEntity.builder()
                .clientId(1L)
                .vehicleId(1L)
                .description("Updated")
                .build();

        when(ownerPersistencePort.findById(1L)).thenReturn(Optional.of(mock(Owner.class)));
        when(vehiclePersistencePort.findById(1L)).thenReturn(Optional.of(mock(Vehicle.class)));
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(99L, incoming))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessage("Service order not found for id: 99");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_updating_service_order_with_invalid_client() {
        ServiceOrderEntity incoming = ServiceOrderEntity.builder()
                .clientId(99L)
                .vehicleId(1L)
                .description("Updated")
                .build();

        when(ownerPersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(5L, incoming))
                .isInstanceOf(ServiceOrderClientNotFoundException.class);

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_updating_service_order_with_invalid_vehicle() {
        ServiceOrderEntity incoming = ServiceOrderEntity.builder()
                .clientId(1L)
                .vehicleId(99L)
                .description("Updated")
                .build();

        when(ownerPersistencePort.findById(1L)).thenReturn(Optional.of(mock(Owner.class)));
        when(vehiclePersistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(5L, incoming))
                .isInstanceOf(ServiceOrderVehicleNotFoundException.class);

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_delete_existing_service_order() {
        when(persistencePort.existsById(5L)).thenReturn(true);

        useCase.delete(5L);

        verify(persistencePort).deleteById(5L);
    }

    @Test
    void should_throw_when_deleting_non_existing_service_order() {
        when(persistencePort.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.delete(99L))
                .isInstanceOf(ServiceOrderNotFoundException.class)
                .hasMessage("Service order not found for id: 99");

        verify(persistencePort, never()).deleteById(any());
    }

    // Testes adicionais para cobertura de branches - scroll com filtros
    @Test
    void should_scroll_by_status_when_filter_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("PENDING", null, null, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll("PENDING", null, null, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_scroll_by_client_id_when_filter_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).clientId(5L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, 5L, null, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll(null, 5L, null, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_scroll_by_vehicle_id_when_filter_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).vehicleId(10L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, null, 10L, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll(null, null, 10L, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_scroll_by_client_id_and_vehicle_id_when_both_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).clientId(5L).vehicleId(10L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll(null, 5L, 10L, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll(null, 5L, 10L, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_scroll_by_client_id_and_status_when_both_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).clientId(5L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("PENDING", 5L, null, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll("PENDING", 5L, null, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_scroll_by_vehicle_id_and_status_when_both_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).vehicleId(10L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("PENDING", null, 10L, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll("PENDING", null, 10L, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_scroll_with_all_filters_when_provided() {
        ScrollPage<ServiceOrder> expected = ScrollPage.<ServiceOrder>builder()
                .data(List.of(ServiceOrderEntity.builder().id(1L).clientId(5L).vehicleId(10L).build()))
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("PENDING", 5L, 10L, 10, "0")).thenReturn(expected);

        ScrollPage<ServiceOrder> actual = useCase.scroll("PENDING", 5L, 10L, 10, "0");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_no_matching_when_scroll_result_is_empty_with_all_filters() {
        ScrollPage<ServiceOrder> empty = ScrollPage.<ServiceOrder>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("PENDING", 5L, 10L, 10, "0")).thenReturn(empty);

        assertThatThrownBy(() -> useCase.scroll("PENDING", 5L, 10L, 10, "0"))
                .isInstanceOf(NoMatchingServiceOrdersException.class)
                .hasMessageStartingWith("No matching service orders");

        verify(persistencePort, never()).save(any());
    }
}
