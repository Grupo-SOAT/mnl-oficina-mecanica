package br.com.fiap.postech.domain.reporting.exception;

import br.com.fiap.postech.domain.reporting.exception.reason.ReportingExceptionReason;

public class ReportingServiceNotFoundException extends RuntimeException {
    public ReportingExceptionReason reason = ReportingExceptionReason.SERVICE_NOT_FOUND;

    public ReportingServiceNotFoundException(Long catalogServiceId) {
        super("Could not find any service for catalog service id: " + catalogServiceId);
    }
}
