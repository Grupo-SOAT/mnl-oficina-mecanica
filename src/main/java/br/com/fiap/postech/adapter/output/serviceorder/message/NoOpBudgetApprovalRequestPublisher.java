package br.com.fiap.postech.adapter.output.serviceorder.message;

import br.com.fiap.postech.port.message.serviceorder.BudgetApprovalRequestPublisherPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.budget.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpBudgetApprovalRequestPublisher implements BudgetApprovalRequestPublisherPort {

    @Override
    public void publish(Long serviceOrderId, String token) {
    }
}
