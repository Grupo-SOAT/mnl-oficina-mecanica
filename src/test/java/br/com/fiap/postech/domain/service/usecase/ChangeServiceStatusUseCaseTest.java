package br.com.fiap.postech.domain.service.usecase;

import br.com.fiap.postech.adapter.output.service.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeServiceStatusUseCaseTest {

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @Mock
    private SupplyPersistencePort supplyPersistencePort;

    @InjectMocks
    private ChangeServiceStatusUseCase useCase;

    @Test
    void should_start_service_decrement_reserved_supply_and_report_in_progress_as_first_service() {
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
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service));

        AtomicReference<ServiceOrderStatus> reportedStatus = new AtomicReference<>();
        var updated = useCase.startService(10L, 1L, reportedStatus::set);

        assertThat(updated.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updated.getStartedAt()).isNotNull();
        assertThat(supply.getReservedQuantity()).isEqualTo(5);
        assertThat(reportedStatus.get()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
        verify(supplyPersistencePort).save(supply);
    }

    @Test
    void should_not_report_in_progress_when_another_service_is_already_in_progress() {
        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("AWAITING_APPROVAL")
                .build();

        var otherInProgress = ServiceEntity.builder()
                .id(2L)
                .serviceOrderId(10L)
                .status("IN_PROGRESS")
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service, otherInProgress));

        AtomicReference<ServiceOrderStatus> reportedStatus = new AtomicReference<>();
        useCase.startService(10L, 1L, reportedStatus::set);

        assertThat(reportedStatus.get()).isNull();
    }

    @Test
    void should_throw_when_decrementing_reserved_supply_would_go_negative_and_not_report_transition() {
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

        AtomicReference<ServiceOrderStatus> reportedStatus = new AtomicReference<>();
        assertThatThrownBy(() -> useCase.startService(10L, 1L, reportedStatus::set))
                .isInstanceOf(NegativeSupplyQuantityException.class);

        assertThat(reportedStatus.get()).isNull();
        verify(servicePersistencePort, never()).save(any());
        verify(supplyPersistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_service_not_found() {
        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.startService(10L, 1L, status -> { }))
                .isInstanceOf(ServiceNotFoundException.class);
    }

    @Test
    void should_complete_service_and_report_completed_when_all_services_done() {
        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("IN_PROGRESS")
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service));

        AtomicReference<ServiceOrderStatus> reportedStatus = new AtomicReference<>();
        var updated = useCase.completeService(10L, 1L, reportedStatus::set);

        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
        assertThat(updated.getCompletedAt()).isNotNull();
        assertThat(reportedStatus.get()).isEqualTo(ServiceOrderStatus.COMPLETED);
    }

    @Test
    void should_report_completed_when_all_services_done_including_cancelled() {
        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("IN_PROGRESS")
                .build();

        var cancelled = ServiceEntity.builder()
                .id(2L)
                .serviceOrderId(10L)
                .status("CANCELLED")
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service, cancelled));

        AtomicReference<ServiceOrderStatus> reportedStatus = new AtomicReference<>();
        useCase.completeService(10L, 1L, reportedStatus::set);

        assertThat(reportedStatus.get()).isEqualTo(ServiceOrderStatus.COMPLETED);
    }

    @Test
    void should_not_report_completed_when_another_service_is_still_in_progress() {
        var service = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .status("IN_PROGRESS")
                .build();

        var otherInProgress = ServiceEntity.builder()
                .id(2L)
                .serviceOrderId(10L)
                .status("IN_PROGRESS")
                .build();

        when(servicePersistencePort.findByIdAndServiceOrderId(1L, 10L)).thenReturn(Optional.of(service));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(10L)).thenReturn(List.of(service, otherInProgress));

        AtomicReference<ServiceOrderStatus> reportedStatus = new AtomicReference<>();
        useCase.completeService(10L, 1L, reportedStatus::set);

        assertThat(reportedStatus.get()).isNull();
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
}
