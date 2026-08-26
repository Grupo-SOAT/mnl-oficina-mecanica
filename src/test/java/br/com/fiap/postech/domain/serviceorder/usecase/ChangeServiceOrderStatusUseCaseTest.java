package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.input.api.model.ServiceOrderAction;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.service.usecase.ChangeServiceStatusUseCase;
import br.com.fiap.postech.domain.serviceorder.exception.PartialBudgetRejectionNotImplementedException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatusChanged;
import br.com.fiap.postech.port.monitoring.ServiceOrderObservabilityPort;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangeServiceOrderStatusUseCaseTest {

    @Mock
    private ServiceOrderPersistencePort serviceOrderPersistencePort;

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @Mock
    private ChangeServiceStatusUseCase changeServiceStatusUseCase;

    @Mock
    private FinalizeInspectionUseCase finalizeInspectionUseCase;

    @Mock
    private EstimateServiceOrderAmountUseCase estimateServiceOrderAmountUseCase;

    @Mock
    private ServiceOrderObservabilityPort serviceOrderObservabilityPort;

    @Mock
    private ServiceOrderStatusLabelPort statusLabelPort;

    @InjectMocks
    private ChangeServiceOrderStatusUseCase useCase;

    @Test
    void should_change_pending_to_in_inspection_and_set_inspected_at() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("PENDING")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.registerProgress(1L, ServiceOrderAction.START_INSPECTION);

        assertThat(updated.getStatus()).isEqualTo("IN_INSPECTION");
        assertThat(updated.getInspectedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(serviceOrderObservabilityPort).recordStatusTransition(any(ServiceOrderStatusChanged.class));
    }

    @Test
    void should_change_in_inspection_to_awaiting_approval() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("IN_INSPECTION")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.registerProgress(1L, ServiceOrderAction.COMPLETE_INSPECTION);

        assertThat(updated.getStatus()).isEqualTo("AWAITING_APPROVAL");
        assertThat(updated.getUpdatedAt()).isNotNull();
        verify(estimateServiceOrderAmountUseCase).estimate(1L);
    }

    @Test
    void should_change_awaiting_approval_to_approved() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of());

        var updated = useCase.registerClientDecision(1L, BudgetDecision.APPROVE);

        assertThat(updated.getStatus()).isEqualTo("APPROVED");
        assertThat(updated.getApprovedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_change_awaiting_approval_to_cancelled() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of());

        var updated = useCase.registerClientDecision(1L, BudgetDecision.CANCEL);

        assertThat(updated.getStatus()).isEqualTo("CANCELLED");
        assertThat(updated.getCancelledAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_throw_not_implemented_for_partial_reject() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));

        assertThatThrownBy(() -> useCase.registerClientDecision(1L, BudgetDecision.PARTIALLY_REJECT))
                .isInstanceOf(PartialBudgetRejectionNotImplementedException.class);

        verify(serviceOrderPersistencePort, never()).save(any());
    }

    @Test
    void should_throw_when_service_order_not_found_for_progress() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.registerProgress(1L, ServiceOrderAction.START_INSPECTION))
                .isInstanceOf(ServiceOrderNotFoundException.class);
    }

    @Test
    void should_throw_when_invalid_transition_is_requested() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("PENDING")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));

        assertThatThrownBy(() -> useCase.registerClientDecision(1L, BudgetDecision.APPROVE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status change not allowed");
    }

    @Test
    void should_reverberate_approve_to_awaiting_approval_services() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var service1 = ServiceEntity.builder()
                .id(10L)
                .serviceOrderId(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var service2 = ServiceEntity.builder()
                .id(11L)
                .serviceOrderId(1L)
                .status("AWAITING_APPROVAL")
                .build();

        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of(service1, service2));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.registerClientDecision(1L, BudgetDecision.APPROVE);

        assertThat(updated.getStatus()).isEqualTo("APPROVED");
        verify(servicePersistencePort).save(service1);
        verify(servicePersistencePort).save(service2);
        assertThat(service1.getStatus()).isEqualTo("APPROVED");
        assertThat(service2.getStatus()).isEqualTo("APPROVED");
        assertThat(service1.getApprovedAt()).isNotNull();
        assertThat(service2.getApprovedAt()).isNotNull();
    }

    @Test
    void should_reverberate_cancel_to_awaiting_approval_services() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var service1 = ServiceEntity.builder()
                .id(10L)
                .serviceOrderId(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var service2 = ServiceEntity.builder()
                .id(11L)
                .serviceOrderId(1L)
                .status("AWAITING_APPROVAL")
                .build();

        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of(service1, service2));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.registerClientDecision(1L, BudgetDecision.CANCEL);

        assertThat(updated.getStatus()).isEqualTo("CANCELLED");
        verify(servicePersistencePort).save(service1);
        verify(servicePersistencePort).save(service2);
        assertThat(service1.getStatus()).isEqualTo("CANCELLED");
        assertThat(service2.getStatus()).isEqualTo("CANCELLED");
        assertThat(service1.getCancelledAt()).isNotNull();
        assertThat(service2.getCancelledAt()).isNotNull();
    }

    @Test
    void should_not_reverberate_to_non_awaiting_approval_services() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var approvedService = ServiceEntity.builder()
                .id(10L)
                .serviceOrderId(1L)
                .status("APPROVED")
                .build();
        var cancelledService = ServiceEntity.builder()
                .id(11L)
                .serviceOrderId(1L)
                .status("CANCELLED")
                .build();

        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of(approvedService, cancelledService));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.registerClientDecision(1L, BudgetDecision.APPROVE);

        verify(servicePersistencePort, never()).save(approvedService);
        verify(servicePersistencePort, never()).save(cancelledService);
    }

    @Test
    void should_allow_deliver_from_completed() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("COMPLETED")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.registerProgress(1L, ServiceOrderAction.DELIVER_VEHICLE);

        assertThat(updated.getStatus()).isEqualTo("DELIVERED");
        assertThat(updated.getDeliveredAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_allow_deliver_from_cancelled() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("CANCELLED")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = useCase.registerProgress(1L, ServiceOrderAction.DELIVER_VEHICLE);

        assertThat(updated.getStatus()).isEqualTo("DELIVERED");
        assertThat(updated.getDeliveredAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isNotNull();
    }

    @Test
    void should_deny_deliver_from_pending() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("PENDING")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));

        assertThatThrownBy(() -> useCase.registerProgress(1L, ServiceOrderAction.DELIVER_VEHICLE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status change not allowed");
    }

    @Test
    void should_deny_deliver_from_in_progress() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("IN_PROGRESS")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));

        assertThatThrownBy(() -> useCase.registerProgress(1L, ServiceOrderAction.DELIVER_VEHICLE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Status change not allowed");
    }

    @Test
    void should_delegate_to_finalize_inspection_when_complete_inspection() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("IN_INSPECTION")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.registerProgress(1L, ServiceOrderAction.COMPLETE_INSPECTION);

        verify(estimateServiceOrderAmountUseCase).estimate(1L);
        verify(finalizeInspectionUseCase).finalizeInspection(1L);
    }

    @Test
    void should_transition_os_to_in_progress_when_start_service_reports_first_service() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("APPROVED")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(changeServiceStatusUseCase.startService(eq(1L), eq(5L), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Consumer<ServiceOrderStatus> callback = invocation.getArgument(2);
                    callback.accept(ServiceOrderStatus.IN_PROGRESS);
                    return null;
                });

        var updated = useCase.registerProgress(1L, ServiceOrderAction.START_SERVICE, 5L);

        assertThat(updated.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(updated.getStartedAt()).isNotNull();
        verify(serviceOrderPersistencePort).save(serviceOrder);
    }

    @Test
    void should_keep_os_status_when_start_service_reports_no_transition() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("APPROVED")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(changeServiceStatusUseCase.startService(eq(1L), eq(5L), any())).thenReturn(null);

        var updated = useCase.registerProgress(1L, ServiceOrderAction.START_SERVICE, 5L);

        assertThat(updated.getStatus()).isEqualTo("APPROVED");
        assertThat(updated.getStartedAt()).isNull();
        verify(serviceOrderPersistencePort).save(serviceOrder);
    }

    @Test
    void should_transition_os_to_completed_when_complete_service_reports_all_done() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("IN_PROGRESS")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(changeServiceStatusUseCase.completeService(eq(1L), eq(5L), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Consumer<ServiceOrderStatus> callback = invocation.getArgument(2);
                    callback.accept(ServiceOrderStatus.COMPLETED);
                    return null;
                });

        var updated = useCase.registerProgress(1L, ServiceOrderAction.COMPLETE_SERVICE, 5L);

        assertThat(updated.getStatus()).isEqualTo("COMPLETED");
        assertThat(updated.getCompletedAt()).isNotNull();
        verify(serviceOrderPersistencePort).save(serviceOrder);
    }

    @Test
    void should_transition_os_to_cancelled_on_cancel_service() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("APPROVED")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(changeServiceStatusUseCase.cancelService(eq(1L), eq(5L))).thenReturn(null);

        var updated = useCase.registerProgress(1L, ServiceOrderAction.CANCEL_SERVICE, 5L);

        assertThat(updated.getStatus()).isEqualTo("CANCELLED");
        assertThat(updated.getCancelledAt()).isNotNull();
        verify(serviceOrderPersistencePort).save(serviceOrder);
    }
}
