package br.com.fiap.postech.domain.service.exception;

public class NoMatchingServicesException extends RuntimeException {
    public NoMatchingServicesException(Long serviceOrderId) {
        super("No matching services for service order id: " + serviceOrderId);
    }
}
