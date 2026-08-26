package br.com.fiap.postech.adapter.output.monitoring;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatusChanged;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MicrometerServiceOrderMetricsAdapterTest {

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final ServiceOrderObservabilityAdapter adapter = new ServiceOrderObservabilityAdapter(meterRegistry);

    @AfterEach
    void tearDown() {
        ((Logger) LoggerFactory.getLogger(ServiceOrderObservabilityAdapter.class)).detachAndStopAllAppenders();
    }

    @Test
    void should_increment_created_counter_tagged_by_today_date() {
        adapter.recordServiceOrderCreated(10L);

        var counter = meterRegistry.get("service_order.created")
                .tag("date", LocalDate.now().toString())
                .counter();
        assertThat(counter.count()).isEqualTo(1);
    }

    @Test
    void should_record_transition_timer_tagged_by_statuses_with_duration() {
        var now = LocalDateTime.now();
        var transition = new ServiceOrderStatusChanged(
                10L,
                ServiceOrderStatus.APPROVED,
                ServiceOrderStatus.IN_PROGRESS,
                now.minusSeconds(120),
                now
        );

        adapter.recordStatusTransition(transition);

        var timer = meterRegistry.get("service_order.status.duration")
                .tag("from_status", "APPROVED")
                .tag("to_status", "IN_PROGRESS")
                .timer();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(TimeUnit.SECONDS)).isCloseTo(120.0, within(0.5));
    }

    @Test
    void should_log_transition_message() {
        var now = LocalDateTime.now();
        var transition = new ServiceOrderStatusChanged(
                10L,
                ServiceOrderStatus.APPROVED,
                ServiceOrderStatus.IN_PROGRESS,
                now.minusSeconds(30),
                now
        );
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(ServiceOrderObservabilityAdapter.class)).addAppender(appender);

        adapter.recordStatusTransition(transition);

        assertThat(appender.list)
                .anySatisfy(event -> {
                    assertThat(event.getLevel()).isEqualTo(Level.INFO);
                    assertThat(event.getFormattedMessage()).isEqualTo("service_order status transitioned");
                });
    }

    @Test
    void should_increment_budget_decision_failure_counter() {
        adapter.recordBudgetDecisionProcessingFailure(10L, "APPROVE", new RuntimeException("boom"));

        assertThat(meterRegistry.get("budget_decision.processing.failures").counter().count()).isEqualTo(1);
    }

    @Test
    void should_increment_budget_approval_publish_failure_counter() {
        adapter.recordBudgetApprovalRequestPublishFailure(10L, new RuntimeException("boom"));

        assertThat(meterRegistry.get("budget_approval_request.publish.failures").counter().count()).isEqualTo(1);
    }
}
