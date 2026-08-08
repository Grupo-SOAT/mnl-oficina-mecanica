package br.com.fiap.postech.domain.catalogservices.exception;

import br.com.fiap.postech.domain.catalogservices.exception.reason.CatalogServicesExceptionReason;

public class CatalogServiceNotFoundException extends RuntimeException {
    public final CatalogServicesExceptionReason reason = CatalogServicesExceptionReason.CATALOG_SERVICE_NOT_FOUND;

    public CatalogServiceNotFoundException(Long id) {
        super("Catalog services not found for id: " + id);
    }
}
