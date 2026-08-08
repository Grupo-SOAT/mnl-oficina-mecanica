package br.com.fiap.postech.domain.catalogservices.exception;

import br.com.fiap.postech.domain.catalogservices.exception.reason.CatalogServicesExceptionReason;

public class InvalidSupplyQuantityException extends RuntimeException {
    public final CatalogServicesExceptionReason reason = CatalogServicesExceptionReason.INVALID_SUPPLY_QUANTITY;

    public InvalidSupplyQuantityException() {
        super("Needed supply quantity must be greater than zero");
    }
}
