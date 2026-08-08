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
}
