package br.com.fiap.postech.domain.serviceorder.exception;

import br.com.fiap.postech.domain.serviceorder.exception.reason.ServiceOrderExceptionReason;

public class ServiceOrderClientNotFoundException extends RuntimeException {
    public final ServiceOrderExceptionReason reason = ServiceOrderExceptionReason.SERVICE_ORDER_CLIENT_NOT_FOUND;

    public ServiceOrderClientNotFoundException(Long clientId) {
        super("Client not found for id: " + clientId);
    }
}
