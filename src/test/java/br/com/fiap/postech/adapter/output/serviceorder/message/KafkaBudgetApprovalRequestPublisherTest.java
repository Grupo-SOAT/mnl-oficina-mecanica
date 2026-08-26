package br.com.fiap.postech.adapter.output.serviceorder.message;

import br.com.fiap.postech.port.monitoring.ServiceOrderObservabilityPort;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KafkaBudgetApprovalRequestPublisherTest {

    @Test
    void should_record_publish_failure_when_send_fails() {
        KafkaTemplate<String, Object> kafkaTemplate = mock(KafkaTemplate.class);
        ServiceOrderObservabilityPort metrics = mock(ServiceOrderObservabilityPort.class);
        var publisher = new KafkaBudgetApprovalRequestPublisher(kafkaTemplate, metrics);

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        publisher.publish(10L, "token");

        verify(metrics).recordBudgetApprovalRequestPublishFailure(eq(10L), any(Throwable.class));
    }
}
