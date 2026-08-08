package br.com.fiap.postech.adapter.input.reporting.controller;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.domain.reporting.exception.ReportingServiceNotFoundException;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.domain.reporting.usecase.ServiceReportingUseCase;
import br.com.fiap.postech.port.reporting.catalogservice.ServiceReportingPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportsControllerTest {
    @Mock
    private ServiceReportingUseCase serviceReportingUseCase;

    @Mock
    private ServiceReportingPort serviceReportingPort;

    @InjectMocks
    private ReportsController controller;

    @ParameterizedTest
    @MethodSource("singleServiceAverageTimeScenarios")
    void should_return_pdf_resource_when_get_service_average_time(
            Long serviceId,
            ServiceCalculatedAverageTime calculatedData,
            byte[] pdfBytes
    ) {
        when(serviceReportingUseCase.calculateAverageTime(serviceId)).thenReturn(calculatedData);
        when(serviceReportingPort.writePDF(calculatedData)).thenReturn(pdfBytes);

        ResponseEntity<Resource> result = controller.getServiceAverageTime(serviceId);
        ResponseEntity<Resource> expectedResult = ResponseEntity.ok(new ByteArrayResource(pdfBytes));

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("headers")
                .isEqualTo(expectedResult);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(result.getHeaders().getFirst("Content-Disposition"))
                .startsWith("attachment; filename=\"service-" + serviceId + "-average-time")
                .endsWith(".pdf\"");

        verify(serviceReportingUseCase).calculateAverageTime(serviceId);
        verify(serviceReportingPort).writePDF(calculatedData);
    }

    @ParameterizedTest
    @MethodSource("allServicesAverageTimeScenarios")
    void should_return_csv_when_get_all_services_average_time(
            List<ServiceCalculatedAverageTime> calculatedData,
            String csv
    ) {
        when(serviceReportingUseCase.calculateAverageTime()).thenReturn(calculatedData);
        when(serviceReportingPort.writeCSV(calculatedData)).thenReturn(csv);

        ResponseEntity<String> result = controller.getAllServicesAverageTime();
        ResponseEntity<String> expectedResult = ResponseEntity.ok(csv);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("headers")
                .isEqualTo(expectedResult);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("text/csv"));
        assertThat(result.getHeaders().getFirst("Content-Disposition"))
                .startsWith("attachment; filename=\"services-average-time_")
                .endsWith(".csv\"");

        verify(serviceReportingUseCase).calculateAverageTime();
        verify(serviceReportingPort).writeCSV(calculatedData);
    }

    @Test
    void should_return_no_content_when_no_matching_services() {
        ResponseEntity<Void> response = controller.handleNoMatchingServices();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_return_not_found_with_error_response_when_service_not_found() {
        ReportingServiceNotFoundException exception = new ReportingServiceNotFoundException(99L);

        ResponseEntity<ErrorResponse> response = controller.handleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().getReason()).isEqualTo(exception.reason.name());
        assertThat(response.getBody().getMessage()).isEqualTo(exception.getMessage());
    }

    private static Stream<Arguments> singleServiceAverageTimeScenarios() {
        return Stream.of(
                Arguments.of(
                        1L,
                        ServiceCalculatedAverageTime.builder()
                                .id(1L)
                                .name("Troca de oleo")
                                .totalCreated(10L)
                                .totalCompleted(8L)
                                .averageTimeBetweenCreateAndComplete(10.4)
                                .averageTimeBetweenStartAndComplete(8.3)
                                .averageTimeBetweenApproveAndComplete(2.1)
                                .averageTimeAwaitingBudgetApproval(1.7)
                                .build(),
                        new byte[]{1, 2, 3}
                ),
                Arguments.of(
                        77L,
                        ServiceCalculatedAverageTime.builder()
                                .id(77L)
                                .name("Alinhamento")
                                .totalCreated(10L)
                                .totalCompleted(8L)
                                .averageTimeBetweenCreateAndComplete(10.4)
                                .averageTimeBetweenStartAndComplete(8.3)
                                .averageTimeBetweenApproveAndComplete(2.1)
                                .averageTimeAwaitingBudgetApproval(1.7)
                                .build(),
                        new byte[]{10, 20, 30, 40}
                )
        );
    }

    private static Stream<Arguments> allServicesAverageTimeScenarios() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                ServiceCalculatedAverageTime.builder()
                                        .id(5L)
                                        .name("Freio")
                                        .totalCreated(10L)
                                        .build()
                        ),
                        "ID;Nome;Total\n5;Freio;10"
                ),
                Arguments.of(
                        List.of(
                                ServiceCalculatedAverageTime.builder()
                                        .id(2L)
                                        .name("Suspensao")
                                        .totalCreated(10L)
                                        .build(),
                                ServiceCalculatedAverageTime.builder()
                                        .id(3L)
                                        .name("Pintura")
                                        .totalCreated(10L)
                                        .build()
                        ),
                        "ID;Nome;Total\n2;Suspensao;10\n3;Pintura;10"
                )
        );
    }
}
