package br.com.fiap.postech.domain.serviceorder.exception;

import br.com.fiap.postech.domain.serviceorder.exception.reason.ServiceOrderExceptionReason;

public class ServiceOrderNotFoundException extends RuntimeException {
    public final ServiceOrderExceptionReason reason = ServiceOrderExceptionReason.SERVICE_ORDER_NOT_FOUND;

    public ServiceOrderNotFoundException(Long id) {
        super("Service order not found for id: " + id);
    }
}
