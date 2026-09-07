package br.com.fiap.postech.config;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterFilter actuatorHttpMetricsFilter() {
        return MeterFilter.deny(id ->
                "http.server.requests".equals(id.getName())
                        && id.getTag("uri") != null
                        && id.getTag("uri").startsWith("/actuator"));
    }
}
