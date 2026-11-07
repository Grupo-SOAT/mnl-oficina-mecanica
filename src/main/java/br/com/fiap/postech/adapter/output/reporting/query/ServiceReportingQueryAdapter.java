package br.com.fiap.postech.adapter.output.reporting.query;

import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.port.reporting.ServiceReportingQueryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Reads the average time report data from {@code ms-pedidos}, which owns service execution.
 */
@Component
public class ServiceReportingQueryAdapter implements ServiceReportingQueryPort {

    private final RestClient restClient;

    public ServiceReportingQueryAdapter(
            RestClient.Builder builder,
            @Value("${app.service-order.base-url:http://localhost:8082}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Optional<ServiceCalculatedAverageTime> findAverageTime(Long catalogServiceId) {
        try {
            return Optional.ofNullable(restClient.get()
                    .uri("/reports/catalog/services/{id}/average-time", catalogServiceId)
                    .retrieve()
                    .body(ServiceCalculatedAverageTime.class));
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw exception;
        }
    }

    @Override
    public List<ServiceCalculatedAverageTime> findAverageTimes() {
        ServiceCalculatedAverageTime[] response = restClient.get()
                .uri("/reports/services-average-time")
                .retrieve()
                .body(ServiceCalculatedAverageTime[].class);
        return response == null ? List.of() : Arrays.asList(response);
    }
}
