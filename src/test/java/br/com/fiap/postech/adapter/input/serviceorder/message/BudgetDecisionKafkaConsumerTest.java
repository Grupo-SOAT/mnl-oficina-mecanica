package br.com.fiap.postech.adapter.input.serviceorder.message;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.input.serviceorder.message.event.BudgetDecisionEvent;
import br.com.fiap.postech.domain.serviceorder.usecase.ProcessBudgetDecisionUseCase;
import br.com.fiap.postech.port.monitoring.ServiceOrderObservabilityPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BudgetDecisionKafkaConsumerTest {

    private final ProcessBudgetDecisionUseCase processBudgetDecisionUseCase = mock(ProcessBudgetDecisionUseCase.class);
    private final ServiceOrderObservabilityPort serviceOrderObservabilityPort = mock(ServiceOrderObservabilityPort.class);
    private final BudgetDecisionKafkaConsumer consumer =
            new BudgetDecisionKafkaConsumer(processBudgetDecisionUseCase, serviceOrderObservabilityPort);

    @Test
    void should_record_failure_and_rethrow_on_invalid_decision() {
        var event = new BudgetDecisionEvent(1L, "NOT_A_DECISION");

        assertThatThrownBy(() -> consumer.consume(event, "1"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(serviceOrderObservabilityPort).recordBudgetDecisionProcessingFailure(eq(1L), eq("NOT_A_DECISION"), any(Throwable.class));
        verify(processBudgetDecisionUseCase, never()).process(any(), any());
    }

    @Test
    void should_not_record_failure_on_successful_processing() {
        var event = new BudgetDecisionEvent(1L, "APPROVE");

        consumer.consume(event, "1");

        verify(processBudgetDecisionUseCase).process(1L, BudgetDecision.APPROVE);
        verify(serviceOrderObservabilityPort, never()).recordBudgetDecisionProcessingFailure(any(), any(), any());
    }
}
