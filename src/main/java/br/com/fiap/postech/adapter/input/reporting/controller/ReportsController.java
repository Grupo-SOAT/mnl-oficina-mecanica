package br.com.fiap.postech.adapter.input.reporting.controller;

import br.com.fiap.postech.adapter.input.api.model.ErrorResponse;
import br.com.fiap.postech.adapter.input.reporting.model.ReportType;
import br.com.fiap.postech.domain.reporting.exception.ReportingNoMatchingServiceException;
import br.com.fiap.postech.domain.reporting.exception.ReportingServiceNotFoundException;
import br.com.fiap.postech.domain.reporting.usecase.ServiceReportingUseCase;
import br.com.fiap.postech.port.api.ReportsApi;
import br.com.fiap.postech.port.reporting.catalogservice.ServiceReportingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
public class ReportsController implements ReportsApi {
    private static final DateTimeFormatter FILENAME_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final String SINGLE_SERVICE_FILENAME_BASE_FORMAT = "service-%d-average-time_%s";
    private static final String ALL_SERVICES_FILENAME_BASE_FORMAT = "services-average-time_%s";

    private final ServiceReportingUseCase serviceReportingUseCase;
    private final ServiceReportingPort serviceReportingPort;

    @Override
    public ResponseEntity<Resource> getServiceAverageTime(Long id) {
        final var data = serviceReportingUseCase.calculateAverageTime(id);
        final var bytes = serviceReportingPort.writePDF(data);
        final var pdf = new ByteArrayResource(bytes);
        final var baseName = String.format(SINGLE_SERVICE_FILENAME_BASE_FORMAT, id, getTimestamp());

        return buildReportResponse(pdf, baseName, ReportType.PDF);
    }

    @Override
    public ResponseEntity<String> getAllServicesAverageTime() {
        final var data = serviceReportingUseCase.calculateAverageTime();
        final var csv = serviceReportingPort.writeCSV(data);
        final var baseName = String.format(ALL_SERVICES_FILENAME_BASE_FORMAT, getTimestamp());

        return buildReportResponse(csv, baseName, ReportType.CSV);
    }

    private <T> ResponseEntity<T> buildReportResponse(T body, String baseName, ReportType reportType) {
        final var contentDisposition = ContentDisposition.attachment()
                .filename(reportType.buildFilename(baseName))
                .build();

        return ResponseEntity.ok()
                .headers(headers -> headers.setContentDisposition(contentDisposition))
                .contentType(reportType.getMediaType())
                .body(body);
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(FILENAME_TIMESTAMP_FORMATTER);
    }

    @ExceptionHandler(ReportingNoMatchingServiceException.class)
    public ResponseEntity<Void> handleNoMatchingServices() {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ReportingServiceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ReportingServiceNotFoundException exception) {
        final var httpStatus = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(httpStatus)
                .body(new ErrorResponse(httpStatus.value(), exception.reason.name(), exception.getMessage()));
    }
}
