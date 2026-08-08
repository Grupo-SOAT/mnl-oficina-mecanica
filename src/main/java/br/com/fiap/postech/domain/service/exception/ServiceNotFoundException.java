package br.com.fiap.postech.domain.service.exception;

import br.com.fiap.postech.domain.service.exception.reason.ServiceExceptionReason;

public class ServiceNotFoundException extends RuntimeException {
    public final ServiceExceptionReason reason = ServiceExceptionReason.SERVICE_NOT_FOUND;

    public ServiceNotFoundException(Long id) {
        super("Service not found for id: " + id);
    }
}
