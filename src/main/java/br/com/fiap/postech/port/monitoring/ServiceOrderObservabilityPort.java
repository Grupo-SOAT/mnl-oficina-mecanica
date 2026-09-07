package br.com.fiap.postech.port.monitoring;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatusChanged;

public interface ServiceOrderObservabilityPort {

    void recordServiceOrderCreated(Long soId);

    void recordStatusTransition(ServiceOrderStatusChanged statusChanged);

    void recordBudgetDecisionProcessingFailure(Long soId, String decision, Throwable cause);

    void recordBudgetApprovalRequestPublishFailure(Long soId, Throwable cause);
}
