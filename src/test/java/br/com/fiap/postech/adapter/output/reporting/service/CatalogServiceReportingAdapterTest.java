package br.com.fiap.postech.adapter.output.reporting.service;

import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceReportingAdapterTest {
    private static final String CSV_HEADER = "ID;Nome;Total Criado;Total Concluído;Média criação-conclusão;" +
            "Média início-conclusão;Média aprovação-conclusão;Média aguardando aprovação";

    @Mock
    private TemplateEngine templateEngine;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ObjectProvider<ServletWebServerApplicationContext> applicationContextProvider;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServletWebServerApplicationContext applicationContext;

    @Mock
    private WebServer webServer;

    @InjectMocks
    private ServiceReportingAdapter adapter;

    @ParameterizedTest
    @MethodSource("writeCSVScenarios")
    void should_write_csv_with_expected_format(List<ServiceCalculatedAverageTime> items, String expectedResult) {
        String result = adapter.writeCSV(items);

        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void should_write_pdf_from_catalog_service_average_time_template() {
        ServiceCalculatedAverageTime input = ServiceCalculatedAverageTime.builder()
                .id(1L)
                .name("Alinhamento")
                .totalCreated(10L)
                .totalCompleted(8L)
                .averageTimeBetweenCreateAndComplete(12.38)
                .averageTimeBetweenStartAndComplete(9.99)
                .averageTimeBetweenApproveAndComplete(4.44)
                .averageTimeAwaitingBudgetApproval(3.09)
                .build();
        when(applicationContextProvider.getIfAvailable()).thenReturn(applicationContext);
        when(applicationContext.getWebServer()).thenReturn(webServer);
        when(webServer.getPort()).thenReturn(8080);
        when(templateEngine.process(eq("catalog-service-average-time-report.html"), any(Context.class)))
                .thenReturn("<html><body><h1>Relatorio</h1></body></html>");

        byte[] result = adapter.writePDF(input);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("catalog-service-average-time-report.html"), contextCaptor.capture());

        Context context = contextCaptor.getValue();

        assertThat(result).isNotEmpty();
        assertThat(new String(result, StandardCharsets.US_ASCII)).startsWith("%PDF");
        assertThat(context.getVariable("id")).isEqualTo(1L);
        assertThat(context.getVariable("name")).isEqualTo("Alinhamento");
        assertThat(context.getVariable("totalCreated")).isEqualTo(10L);
        assertThat(context.getVariable("totalCompleted")).isEqualTo(8L);
        assertThat(context.getVariable("averageTimeBetweenCreateAndComplete")).isEqualTo("12,3H");
        assertThat(context.getVariable("averageTimeBetweenStartAndComplete")).isEqualTo("9,9H");
        assertThat(context.getVariable("averageTimeBetweenApproveAndComplete")).isEqualTo("4,4H");
        assertThat(context.getVariable("averageTimeAwaitingBudgetApproval")).isEqualTo("3,0H");
        assertThat(context.getVariable("baseUrl")).isEqualTo("http://localhost:8080");
        assertThat(context.getVariable("reportGeneratedAt")).isInstanceOf(String.class);
    }

    @Test
    void should_write_pdf_with_localhost_base_url_when_web_server_context_is_unavailable() {
        ServiceCalculatedAverageTime input = ServiceCalculatedAverageTime.builder()
                .id(2L)
                .name("Balanceamento")
                .totalCreated(10L)
                .totalCompleted(8L)
                .averageTimeBetweenCreateAndComplete(11.19)
                .averageTimeBetweenStartAndComplete(7.89)
                .averageTimeBetweenApproveAndComplete(3.66)
                .averageTimeAwaitingBudgetApproval(2.01)
                .build();
        when(applicationContextProvider.getIfAvailable()).thenReturn(null);
        when(templateEngine.process(eq("catalog-service-average-time-report.html"), any(Context.class)))
                .thenReturn("<html><body><h1>Relatorio</h1></body></html>");

        adapter.writePDF(input);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("catalog-service-average-time-report.html"), contextCaptor.capture());
        assertThat(contextCaptor.getValue().getVariable("baseUrl")).isEqualTo("http://localhost");
    }

    private static Stream<Arguments> writeCSVScenarios() {
        final var mock1 = ServiceCalculatedAverageTime.builder()
                .id(7L)
                .name("Freio")
                .totalCreated(10L)
                .totalCompleted(8L)
                .averageTimeBetweenCreateAndComplete(7.49)
                .averageTimeBetweenStartAndComplete(5.11)
                .averageTimeBetweenApproveAndComplete(2.55)
                .averageTimeAwaitingBudgetApproval(1.07)
                .build();
        final var mock2 = ServiceCalculatedAverageTime.builder()
                .id(9L)
                .name("Pintura")
                .totalCreated(10L)
                .totalCompleted(8L)
                .averageTimeBetweenCreateAndComplete(9.90)
                .averageTimeBetweenStartAndComplete(8.01)
                .averageTimeBetweenApproveAndComplete(3.08)
                .averageTimeAwaitingBudgetApproval(4.99)
                .build();

        return Stream.of(
                Arguments.of(
                        List.of(),
                        CSV_HEADER + "\n"
                ),
                Arguments.of(
                        List.of(mock1, mock2),
                        CSV_HEADER + "\n" +
                                "7;Freio;10;8;7,4H;5,1H;2,5H;1,0H\n" +
                                "9;Pintura;10;8;9,9H;8,0H;3,0H;4,9H\n"
                )
        );
    }
}
