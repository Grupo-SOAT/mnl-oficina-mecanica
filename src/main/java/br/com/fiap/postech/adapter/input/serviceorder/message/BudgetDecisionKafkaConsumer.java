package br.com.fiap.postech.adapter.input.serviceorder.message;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.input.serviceorder.message.event.BudgetDecisionEvent;
import br.com.fiap.postech.domain.serviceorder.usecase.ProcessBudgetDecisionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.budget.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class BudgetDecisionKafkaConsumer {

    private final ProcessBudgetDecisionUseCase processBudgetDecisionUseCase;

    @Transactional
    @KafkaListener(topics = "${app.budget.kafka.topic.decision}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(
            BudgetDecisionEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        if (key != null && !key.equals(String.valueOf(event.getServiceOrderId()))) {
            log.warn("Kafka key mismatch: received key={} but event.serviceOrderId={}", key, event.getServiceOrderId());
        }
        processBudgetDecisionUseCase.process(event.getServiceOrderId(), BudgetDecision.valueOf(event.getDecision()));
    }
}
