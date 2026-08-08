package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.port.persistence.serviceorder.BudgetApprovalTokenPersistencePort;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ProcessBudgetDecisionUseCaseTest {

    @Mock
    private ServiceOrderPersistencePort serviceOrderPersistencePort;

    @Mock
    private ServicePersistencePort servicePersistencePort;

    @Mock
    private BudgetApprovalTokenPersistencePort budgetApprovalTokenPersistencePort;

    @Mock
    private FinalizeInspectionUseCase finalizeInspectionUseCase;

    @Mock
    private EstimateServiceOrderAmountUseCase estimateServiceOrderAmountUseCase;

    @Mock
    private ServiceOrderStatusLabelPort statusLabelPort;

    private ProcessBudgetDecisionUseCase useCase;

    @BeforeEach
    void setUp() {
        ChangeServiceOrderStatusUseCase changeStatusUseCase = new ChangeServiceOrderStatusUseCase(
                serviceOrderPersistencePort,
                servicePersistencePort,
                null,
                finalizeInspectionUseCase,
                estimateServiceOrderAmountUseCase,
                statusLabelPort
        );
        useCase = new ProcessBudgetDecisionUseCase(changeStatusUseCase, budgetApprovalTokenPersistencePort);
    }

    @Test
    void should_approve_and_mark_token_used() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of());

        useCase.process(1L, BudgetDecision.APPROVE);

        assertThat(serviceOrder.getStatus()).isEqualTo("APPROVED");
        verify(budgetApprovalTokenPersistencePort).markUsed(1L);
    }

    @Test
    void should_reject_and_mark_token_used() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(serviceOrderPersistencePort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(servicePersistencePort.findAllByServiceOrderId(1L)).thenReturn(List.of());

        useCase.process(1L, BudgetDecision.REJECT);

        assertThat(serviceOrder.getStatus()).isEqualTo("CANCELLED");
        verify(budgetApprovalTokenPersistencePort).markUsed(1L);
    }

    @Test
    void should_not_mark_used_when_service_order_not_found() {
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.process(1L, BudgetDecision.APPROVE))
                .isInstanceOf(ServiceOrderNotFoundException.class);

        verify(budgetApprovalTokenPersistencePort, never()).markUsed(any());
    }
}
