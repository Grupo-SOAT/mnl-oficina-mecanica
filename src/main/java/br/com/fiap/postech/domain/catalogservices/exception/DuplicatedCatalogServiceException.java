package br.com.fiap.postech.domain.catalogservices.exception;

import br.com.fiap.postech.domain.catalogservices.exception.reason.CatalogServicesExceptionReason;

public class DuplicatedCatalogServiceException extends RuntimeException {
    public final CatalogServicesExceptionReason reason = CatalogServicesExceptionReason.CATALOG_SERVICE_CONFLICT_DUPLICATED_NAME;

    public DuplicatedCatalogServiceException(String name) {
        super("Catalog services already exists for name: " + name);
    }
}
