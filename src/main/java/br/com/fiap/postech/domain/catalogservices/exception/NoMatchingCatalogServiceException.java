package br.com.fiap.postech.domain.catalogservices.exception;

import br.com.fiap.postech.domain.catalogservices.exception.reason.CatalogServicesExceptionReason;

public class NoMatchingCatalogServiceException extends RuntimeException {
    public final CatalogServicesExceptionReason reason = CatalogServicesExceptionReason.CATALOG_SERVICE_NOT_FOUND;

    public NoMatchingCatalogServiceException(String name) {
        super("No matching catalog service for name: " + name);
    }
}
