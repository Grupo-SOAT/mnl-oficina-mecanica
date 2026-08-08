package br.com.fiap.postech.domain.service.usecase;

import br.com.fiap.postech.adapter.output.service.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeServiceStatusUseCaseTest {

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @Mock
    private ServiceOrderPersistencePort serviceOrderPersistencePort;

    @Mock
    private SupplyPersistencePort supplyPersistencePort;

    @Mock
    private ServiceStatusLabelPort statusLabelPort;

    @InjectMocks
    private ChangeServiceStatusUseCase useCase;

    @Test
    void should_start_service_and_decrement_reserved_supply() {
        var supply1 = NeededSupplyEntity.builder()
                .idSupply(100L)
                .quantity(5)
                .build();

        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("AWAITING_APPROVAL")
                .neededSupplyEntities(List.of(supply1))
                .build();
        
        var serviceOrder = ServiceOrderEntity.builder()
                .id(10L)
                .status("APPROVED")
                .build();
        
        var supply = SupplyEntity.builder()
                .id(100L)
                .reservedQuantity(10)
                .availableQuantity(20)
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(supplyPersistencePort.findById(100L)).thenReturn(Optional.of(supply));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(serviceOrderPersistencePort.findById(10L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service));

        var updated = useCase.startService(10L, 1L);

        assertThat(updated.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updated.getStartedAt()).isNotNull();
        assertThat(supply.getReservedQuantity()).isEqualTo(5);
        verify(supplyPersistencePort).save(supply);
    }

    @Test
    void should_throw_when_decrementing_reserved_supply_would_go_negative() {
        var supply1 = NeededSupplyEntity.builder()
                .idSupply(100L)
                .quantity(15)
                .build();

        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("AWAITING_APPROVAL")
                .neededSupplyEntities(List.of(supply1))
                .build();

        var supply = SupplyEntity.builder()
                .id(100L)
                .reservedQuantity(10)
                .availableQuantity(20)
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(supplyPersistencePort.findById(100L)).thenReturn(Optional.of(supply));

        assertThatThrownBy(() -> useCase.startService(10L, 1L))
                .isInstanceOf(NegativeSupplyQuantityException.class);

        verify(servicePersistencePort, never()).save(any());
        verify(supplyPersistencePort, never()).save(any());
    }

    @Test
    void should_complete_service_and_update_os_to_completed_when_last_service() {
        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("IN_PROGRESS")
                .build();
        
        var serviceOrder = ServiceOrderEntity.builder()
                .id(10L)
                .status("IN_PROGRESS")
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(serviceOrderPersistencePort.findById(10L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service));

        var updated = useCase.completeService(10L, 1L);

        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
        assertThat(updated.getCompletedAt()).isNotNull();
        assertThat(serviceOrder.getStatus()).isEqualTo("COMPLETED");
        verify(serviceOrderPersistencePort).save(serviceOrder);
    }

    @Test
    void should_cancel_service_and_release_reserved_supplies() {
        var supply1 = NeededSupplyEntity.builder()
                .idSupply(100L)
                .quantity(5)
                .build();

        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("AWAITING_APPROVAL")
                .neededSupplyEntities(List.of(supply1))
                .build();

        var supply = SupplyEntity.builder()
                .id(100L)
                .reservedQuantity(10)
                .availableQuantity(20)
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(supplyPersistencePort.findById(100L)).thenReturn(Optional.of(supply));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.cancelService(10L, 1L);

        assertThat(updated.getStatus()).isEqualTo("CANCELLED");
        assertThat(updated.getCancelledAt()).isNotNull();
        assertThat(supply.getReservedQuantity()).isEqualTo(5);
        assertThat(supply.getAvailableQuantity()).isEqualTo(25);
        verify(supplyPersistencePort).save(supply);
    }

    @Test
    void should_throw_when_service_not_found() {
        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.startService(10L, 1L))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void should_update_os_to_in_progress_when_first_service_starts() {
        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("AWAITING_APPROVAL")
                .build();
        
        var serviceOrder = ServiceOrderEntity.builder()
                .id(10L)
                .status("APPROVED")
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(serviceOrderPersistencePort.findById(10L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service));

        useCase.startService(10L, 1L);

        assertThat(serviceOrder.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(serviceOrder.getStartedAt()).isNotNull();
        verify(serviceOrderPersistencePort).save(serviceOrder);
    }
}
