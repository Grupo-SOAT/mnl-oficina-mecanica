package br.com.fiap.postech.domain.supply.exception;

public class NoMatchingSuppliesException extends RuntimeException {
    public NoMatchingSuppliesException(String sku) {
        super("No matching supplies for sku: " + sku);
    }
}
