package br.com.fiap.postech.adapter.output.serviceorder.message;

import br.com.fiap.postech.port.message.serviceorder.BudgetApprovalRequestPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.budget.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class KafkaBudgetApprovalRequestPublisher implements BudgetApprovalRequestPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.budget.kafka.topic.request}")
    private String topic;

    @Override
    public void publish(Long serviceOrderId, String token) {
        BudgetApprovalRequestEvent event = new BudgetApprovalRequestEvent(serviceOrderId, token);
        kafkaTemplate.send(topic, serviceOrderId.toString(), event);
    }

    public record BudgetApprovalRequestEvent(Long serviceOrderId, String token) {
    }
}
