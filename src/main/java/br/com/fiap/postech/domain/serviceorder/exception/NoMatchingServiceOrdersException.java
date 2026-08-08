package br.com.fiap.postech.domain.serviceorder.exception;

public class NoMatchingServiceOrdersException extends RuntimeException {
    public NoMatchingServiceOrdersException(String filter) {
        super("No matching service orders for filter: " + filter);
    }
}
