package br.com.fiap.postech.config;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsConfigTest {

    private Meter.Id httpRequest(String uri) {
        return Timer.builder("http.server.requests")
                .tags("uri", uri, "method", "GET", "status", "200")
                .register(new SimpleMeterRegistry())
                .getId();
    }

    private MeterFilter filter() {
        return new MetricsConfig().actuatorHttpMetricsFilter();
    }

    @Test
    void should_deny_http_server_requests_for_actuator_endpoints() {
        assertThat(filter().accept(httpRequest("/actuator/health"))).isEqualTo(MeterFilterReply.DENY);
        assertThat(filter().accept(httpRequest("/actuator/prometheus"))).isEqualTo(MeterFilterReply.DENY);
    }

    @Test
    void should_not_deny_http_server_requests_for_api_endpoints() {
        assertThat(filter().accept(httpRequest("/service-orders"))).isEqualTo(MeterFilterReply.NEUTRAL);
        assertThat(filter().accept(httpRequest("/auth/login"))).isEqualTo(MeterFilterReply.NEUTRAL);
    }
}
