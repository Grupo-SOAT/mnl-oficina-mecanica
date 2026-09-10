package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.serviceorder.model.BudgetApprovalToken;
import br.com.fiap.postech.port.message.serviceorder.BudgetApprovalRequestPublisherPort;
import br.com.fiap.postech.port.persistence.serviceorder.BudgetApprovalTokenPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizeInspectionUseCaseTest {

    @Mock
    private ServiceOrderPersistencePort serviceOrderPersistencePort;

    @Mock
    private BudgetApprovalTokenPersistencePort budgetApprovalTokenPersistencePort;

    @Mock
    private BudgetApprovalRequestPublisherPort budgetApprovalRequestPublisherPort;

    private FinalizeInspectionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinalizeInspectionUseCase(
                serviceOrderPersistencePort,
                budgetApprovalTokenPersistencePort,
                budgetApprovalRequestPublisherPort,
                48
        );
    }

    @Test
    void should_generate_token_persist_and_publish_when_finalizing_inspection() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("IN_INSPECTION")
                .build();
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));

        useCase.finalizeInspection(1L);

        ArgumentCaptor<BudgetApprovalToken> tokenCaptor = ArgumentCaptor.forClass(BudgetApprovalToken.class);
        verify(budgetApprovalTokenPersistencePort).create(tokenCaptor.capture());

        BudgetApprovalToken capturedToken = tokenCaptor.getValue();
        assertThat(capturedToken.serviceOrderId()).isEqualTo(1L);
        assertThat(capturedToken.token()).isNotBlank();
        assertThat(capturedToken.expiresAt()).isNotNull();
        assertThat(capturedToken.createdAt()).isNotNull();

        verify(budgetApprovalRequestPublisherPort).publish(1L, capturedToken.token());
    }

    @Test
    void should_reuse_existing_token_without_new_insert_when_inspection_already_finalized() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var existingToken = new BudgetApprovalToken(1L, "token-abc", java.time.Instant.now().plusSeconds(3600));
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(budgetApprovalTokenPersistencePort.findByServiceOrderId(1L)).thenReturn(Optional.of(existingToken));

        useCase.finalizeInspection(1L);

        verify(budgetApprovalTokenPersistencePort, never()).create(any());
        verify(budgetApprovalRequestPublisherPort).publish(1L, "token-abc");
    }

    @Test
    void should_skip_publish_when_existing_token_is_expired() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var now = java.time.Instant.now();
        var expiredToken = new BudgetApprovalToken(
                1L, 1L, "token-expired", now.minusSeconds(3600), now.minusSeconds(7200), null);
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(budgetApprovalTokenPersistencePort.findByServiceOrderId(1L)).thenReturn(Optional.of(expiredToken));

        useCase.finalizeInspection(1L);

        verify(budgetApprovalTokenPersistencePort, never()).create(any());
        verify(budgetApprovalRequestPublisherPort, never()).publish(any(), any());
    }

    @Test
    void should_skip_publish_when_existing_token_is_used() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .status("AWAITING_APPROVAL")
                .build();
        var now = java.time.Instant.now();
        var usedToken = new BudgetApprovalToken(
                1L, 1L, "token-used", now.plusSeconds(3600), now.minusSeconds(60), now.minusSeconds(30));
        when(serviceOrderPersistencePort.findById(1L)).thenReturn(Optional.of(serviceOrder));
        when(budgetApprovalTokenPersistencePort.findByServiceOrderId(1L)).thenReturn(Optional.of(usedToken));

        useCase.finalizeInspection(1L);

        verify(budgetApprovalTokenPersistencePort, never()).create(any());
        verify(budgetApprovalRequestPublisherPort, never()).publish(any(), any());
    }
}
