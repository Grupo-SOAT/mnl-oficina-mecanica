package br.com.fiap.postech.domain.catalogservices.exception;

import br.com.fiap.postech.domain.catalogservices.exception.reason.CatalogServicesExceptionReason;

public class InvalidCatalogServiceNameException extends RuntimeException {
    public final CatalogServicesExceptionReason reason = CatalogServicesExceptionReason.INVALID_CATALOG_SERVICE_NAME;

    public InvalidCatalogServiceNameException() {
        super("Catalog service name must not be blank");
    }
}
