package br.com.fiap.postech.adapter.output.reporting.service;

import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.port.reporting.catalogservice.ServiceReportingPort;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.server.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ServiceReportingAdapter implements ServiceReportingPort {
    private static final String BASE_URL_PROP = "baseUrl";
    private static final String REPORT_GENERATED_AT_PROP = "reportGeneratedAt";
    private static final String LOCALHOST = "http://localhost";
    private static final int BUFFER_SIZE = 100000;
    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final int DECIMAL_PRECISION = 1;
    private static final String DECIMAL_SEPARATOR = ".";
    private static final String BR_DECIMAL_SEPARATOR = ",";
    private static final String HOUR_INDICATOR = "H";

    private static final Map<String, String> TEMPLATE_BY_CLASS_NAME = Map.of(
            ServiceCalculatedAverageTime.class.getSimpleName(), "catalog-service-average-time-report.html"
    );

    private static final String CSV_SEPARATOR = ";";
    private static final String CSV_LINE_BREAK = "\n";
    private static final String CSV_HEADER = String.join(
            CSV_SEPARATOR,
            List.of(
                    "ID",
                    "Nome",
                    "Total Criado",
                    "Total Concluído",
                    "Média criação-conclusão",
                    "Média início-conclusão",
                    "Média aprovação-conclusão",
                    "Média aguardando aprovação"
            )
    );

    private final TemplateEngine templateEngine;
    private final ObjectProvider<ServletWebServerApplicationContext> applicationContextProvider;

    private String getBaseUrl() {
        final var applicationContext = applicationContextProvider.getIfAvailable();
        if (applicationContext == null || applicationContext.getWebServer() == null) {
            return LOCALHOST;
        }

        return LOCALHOST + ':' + applicationContext.getWebServer().getPort();
    }

    private String formatHours(Double value) {
        return BigDecimal.valueOf(value)
                .setScale(DECIMAL_PRECISION, RoundingMode.DOWN)
                .toPlainString()
                .replace(DECIMAL_SEPARATOR, BR_DECIMAL_SEPARATOR)
                .concat(HOUR_INDICATOR);
    }

    private Map<String, Object> toTemplateVariables(ServiceCalculatedAverageTime input) {
        final var variables = new LinkedHashMap<String, Object>();
        variables.put("id", input.id());
        variables.put("name", input.name());
        variables.put("totalCreated", input.totalCreated());
        variables.put("totalCompleted", input.totalCompleted());
        variables.put("averageTimeBetweenCreateAndComplete", formatHours(input.averageTimeBetweenCreateAndComplete()));
        variables.put("averageTimeBetweenStartAndComplete", formatHours(input.averageTimeBetweenStartAndComplete()));
        variables.put("averageTimeBetweenApproveAndComplete", formatHours(input.averageTimeBetweenApproveAndComplete()));
        variables.put("averageTimeAwaitingBudgetApproval", formatHours(input.averageTimeAwaitingBudgetApproval()));

        return variables;
    }

    private String generateHTML(Map<String, Object> variables, String template) {
        final var context = new Context();
        context.setVariables(variables);
        context.setVariable(BASE_URL_PROP, getBaseUrl());
        context.setVariable(REPORT_GENERATED_AT_PROP, LocalDateTime.now().format(REPORT_DATE_FORMATTER));

        return templateEngine.process(template, context);
    }

    private byte[] writePDF(Map<String, Object> variables, String template) {
        final var html = generateHTML(variables, template);
        final var converterProperties = new ConverterProperties();
        converterProperties.setBaseUri(getBaseUrl());

        final var target = new ByteArrayOutputStream(BUFFER_SIZE);
        HtmlConverter.convertToPdf(html, new BufferedOutputStream(target), converterProperties);

        return target.toByteArray();
    }

    @Override
    public byte[] writePDF(ServiceCalculatedAverageTime input) {
        return this.writePDF(
                toTemplateVariables(input),
                TEMPLATE_BY_CLASS_NAME.get(input.getClass().getSimpleName())
        );
    }

    private String writeCSVLine(ServiceCalculatedAverageTime item) {
        return item.id() + CSV_SEPARATOR +
                item.name() + CSV_SEPARATOR +
                item.totalCreated() + CSV_SEPARATOR +
                item.totalCompleted() + CSV_SEPARATOR +
                formatHours(item.averageTimeBetweenCreateAndComplete()) + CSV_SEPARATOR +
                formatHours(item.averageTimeBetweenStartAndComplete()) + CSV_SEPARATOR +
                formatHours(item.averageTimeBetweenApproveAndComplete()) + CSV_SEPARATOR +
                formatHours(item.averageTimeAwaitingBudgetApproval()) + CSV_LINE_BREAK;
    }

    @Override
    public String writeCSV(List<ServiceCalculatedAverageTime> items) {
        var csv = CSV_HEADER + CSV_LINE_BREAK;

        csv += items.stream()
                .map(this::writeCSVLine)
                .collect(Collectors.joining());

        return csv;
    }
}
