package br.com.fiap.postech.adapter.output.monitoring;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatusChanged;
import br.com.fiap.postech.port.monitoring.ServiceOrderObservabilityPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
public class ServiceOrderObservabilityAdapter implements ServiceOrderObservabilityPort {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOrderObservabilityAdapter.class);

    private final MeterRegistry meterRegistry;
    private final Counter serviceOrderCreatedCounter;
    private final Counter budgetDecisionFailureCounter;
    private final Counter budgetApprovalPublishFailureCounter;

    public ServiceOrderObservabilityAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.serviceOrderCreatedCounter = Counter.builder("service_order.created")
                .description("Service orders created")
                .register(meterRegistry);
        this.budgetDecisionFailureCounter = Counter.builder("budget_decision.processing.failures")
                .description("Failed budget decision processing")
                .register(meterRegistry);
        this.budgetApprovalPublishFailureCounter = Counter.builder("budget_approval_request.publish.failures")
                .description("Failed budget approval request publishes")
                .register(meterRegistry);
    }

    @Override
    public void recordServiceOrderCreated(Long soId) {
        String date = LocalDate.now().toString();
        serviceOrderCreatedCounter.increment();
        logger.info("service_order created",
                StructuredArguments.keyValue("so_id", soId),
                StructuredArguments.keyValue("date", date));
    }

    @Override
    public void recordStatusTransition(ServiceOrderStatusChanged statusChanged) {
        long durationSeconds = statusChanged.lastStatusChangedAt() == null
                ? 0L
                : Duration.between(statusChanged.lastStatusChangedAt(), statusChanged.currentStatusChangedAt())
                .toSeconds();

        Timer.builder("service_order.status.duration")
                .description("Time spent in a service order status")
                .tags(
                        "from_status", statusChanged.from().name(),
                        "to_status", statusChanged.to().name())
                .register(meterRegistry)
                .record(durationSeconds, TimeUnit.SECONDS);

        logger.info("service_order status transitioned",
                StructuredArguments.keyValue("so_id", statusChanged.id()),
                StructuredArguments.keyValue("from_status", statusChanged.from().name()),
                StructuredArguments.keyValue("to_status", statusChanged.to().name()),
                StructuredArguments.keyValue("duration_in_status_seconds", durationSeconds));
    }

    @Override
    public void recordBudgetDecisionProcessingFailure(Long soId, String decision, Throwable cause) {
        budgetDecisionFailureCounter.increment();
        logger.error("budget_decision processing failed",
                StructuredArguments.keyValue("so_id", soId),
                StructuredArguments.keyValue("decision", decision),
                cause);
    }

    @Override
    public void recordBudgetApprovalRequestPublishFailure(Long soId, Throwable cause) {
        budgetApprovalPublishFailureCounter.increment();
        logger.error("budget_approval_request publish failed",
                StructuredArguments.keyValue("so_id", soId),
                cause);
    }
}
