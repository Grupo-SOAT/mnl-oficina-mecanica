package br.com.fiap.postech.adapter.output.reporting.query;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ServiceReportingQueryAdapterTest {

    private MockRestServiceServer server;
    private ServiceReportingQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter = new ServiceReportingQueryAdapter(builder, "http://localhost:8082");
    }

    @Test
    void should_find_average_time_by_catalog_service() {
        server.expect(requestTo("http://localhost:8082/reports/catalog/services/7/average-time"))
                .andRespond(withSuccess("""
                        {"id":7,"name":"Oleo","totalCreated":2,"totalCompleted":1,
                         "averageTimeBetweenCreateAndComplete":4.0,
                         "averageTimeBetweenStartAndComplete":2.0,
                         "averageTimeBetweenApproveAndComplete":3.0,
                         "averageTimeAwaitingBudgetApproval":1.0}
                        """, MediaType.APPLICATION_JSON));

        var result = adapter.findAverageTime(7L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(7L);
        assertThat(result.get().totalCreated()).isEqualTo(2L);
    }

    @Test
    void should_return_empty_when_catalog_service_is_missing() {
        server.expect(requestTo("http://localhost:8082/reports/catalog/services/9/average-time"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThat(adapter.findAverageTime(9L)).isEmpty();
    }

    @Test
    void should_find_all_average_times() {
        server.expect(requestTo("http://localhost:8082/reports/services-average-time"))
                .andRespond(withSuccess("""
                        [{"id":1,"name":"Oleo","totalCreated":2,"totalCompleted":1,
                          "averageTimeBetweenCreateAndComplete":4.0,
                          "averageTimeBetweenStartAndComplete":2.0,
                          "averageTimeBetweenApproveAndComplete":3.0,
                          "averageTimeAwaitingBudgetApproval":1.0}]
                        """, MediaType.APPLICATION_JSON));

        assertThat(adapter.findAverageTimes()).hasSize(1);
    }

    @Test
    void should_return_empty_list_when_response_is_null() {
        server.expect(requestTo("http://localhost:8082/reports/services-average-time"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertThat(adapter.findAverageTimes()).isEmpty();
    }
}
