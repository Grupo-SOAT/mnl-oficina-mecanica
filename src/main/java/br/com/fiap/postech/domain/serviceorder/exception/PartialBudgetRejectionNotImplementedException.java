package br.com.fiap.postech.domain.serviceorder.exception;

public class PartialBudgetRejectionNotImplementedException extends RuntimeException {
    public PartialBudgetRejectionNotImplementedException() {
        super("Partial budget rejection is not implemented yet");
    }
}
