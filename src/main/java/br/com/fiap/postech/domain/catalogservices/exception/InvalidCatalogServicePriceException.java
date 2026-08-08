package br.com.fiap.postech.domain.catalogservices.exception;

import br.com.fiap.postech.domain.catalogservices.exception.reason.CatalogServicesExceptionReason;

public class InvalidCatalogServicePriceException extends RuntimeException {
    public final CatalogServicesExceptionReason reason = CatalogServicesExceptionReason.INVALID_CATALOG_SERVICE_PRICE;

    public InvalidCatalogServicePriceException() {
        super("Catalog service base price must be greater than zero");
    }
}
