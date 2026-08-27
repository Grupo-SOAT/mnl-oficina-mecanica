package br.com.fiap.postech.adapter.input.serviceorder.message;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.input.serviceorder.message.event.BudgetDecisionEvent;
import br.com.fiap.postech.domain.serviceorder.usecase.ProcessBudgetDecisionUseCase;
import br.com.fiap.postech.port.monitoring.ServiceOrderObservabilityPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.budget.kafka.enabled", havingValue = "true")
@RequiredArgsConstructor
public class BudgetDecisionKafkaConsumer {

    private final ProcessBudgetDecisionUseCase processBudgetDecisionUseCase;
    private final ServiceOrderObservabilityPort serviceOrderObservabilityPort;

    @KafkaListener(topics = "${app.budget.kafka.topic.decision}", groupId = "${spring.kafka.consumer.group-id}")
    @RetryableTopic(
            attempts = "${app.budget.kafka.retry.attempts:3}",
            backOff = @BackOff(delayString = "${app.budget.kafka.retry.delay-ms:2000}"),
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            kafkaTemplate = "kafkaTemplate",
            listenerContainerFactory = "kafkaListenerContainerFactory")
    public void consume(
            BudgetDecisionEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        if (key != null && !key.equals(String.valueOf(event.getServiceOrderId()))) {
            log.warn("Kafka key mismatch: received key={} but event.serviceOrderId={}", key, event.getServiceOrderId());
        }
        try {
            processBudgetDecisionUseCase.process(event.getServiceOrderId(), BudgetDecision.valueOf(event.getDecision()));
        } catch (RuntimeException ex) {
            serviceOrderObservabilityPort.recordBudgetDecisionProcessingFailure(
                    event.getServiceOrderId(), event.getDecision(), ex);
            throw ex;
        }
    }
}
