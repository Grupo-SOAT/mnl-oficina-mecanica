package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.serviceorder.model.BudgetApprovalToken;
import br.com.fiap.postech.port.message.serviceorder.BudgetApprovalRequestPublisherPort;
import br.com.fiap.postech.port.persistence.serviceorder.BudgetApprovalTokenPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class FinalizeInspectionUseCase {

    private final ServiceOrderPersistencePort serviceOrderPersistencePort;
    private final BudgetApprovalTokenPersistencePort budgetApprovalTokenPersistencePort;
    private final BudgetApprovalRequestPublisherPort budgetApprovalRequestPublisherPort;
    private final int tokenTtlHours;

    public FinalizeInspectionUseCase(
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            BudgetApprovalTokenPersistencePort budgetApprovalTokenPersistencePort,
            BudgetApprovalRequestPublisherPort budgetApprovalRequestPublisherPort,
            int tokenTtlHours
    ) {
        this.serviceOrderPersistencePort = serviceOrderPersistencePort;
        this.budgetApprovalTokenPersistencePort = budgetApprovalTokenPersistencePort;
        this.budgetApprovalRequestPublisherPort = budgetApprovalRequestPublisherPort;
        this.tokenTtlHours = tokenTtlHours;
    }

    public void finalizeInspection(Long serviceOrderId) {
        serviceOrderPersistencePort.findById(serviceOrderId)
                .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        Optional<BudgetApprovalToken> existing =
                budgetApprovalTokenPersistencePort.findByServiceOrderId(serviceOrderId);
        if (existing.isPresent()) {
            BudgetApprovalToken token = existing.get();
            if (token.usedAt() == null && token.expiresAt().isAfter(Instant.now())) {
                budgetApprovalRequestPublisherPort.publish(serviceOrderId, token.token());
            }
            return;
        }

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds((long) tokenTtlHours * 3600L);
        budgetApprovalTokenPersistencePort.create(new BudgetApprovalToken(serviceOrderId, token, expiresAt));
        budgetApprovalRequestPublisherPort.publish(serviceOrderId, token);
    }
}
