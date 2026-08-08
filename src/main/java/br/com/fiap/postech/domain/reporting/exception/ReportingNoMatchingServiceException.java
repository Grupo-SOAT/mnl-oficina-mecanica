package br.com.fiap.postech.domain.reporting.exception;

public class ReportingNoMatchingServiceException extends RuntimeException {
    public ReportingNoMatchingServiceException() {
        super("Could not find any service to generate report");
    }
}
