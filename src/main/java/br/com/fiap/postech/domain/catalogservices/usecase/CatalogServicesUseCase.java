package br.com.fiap.postech.domain.catalogservices.usecase;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.domain.catalogservices.exception.DuplicatedCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServiceNameException;
import br.com.fiap.postech.domain.catalogservices.exception.InvalidCatalogServicePriceException;
import br.com.fiap.postech.domain.catalogservices.exception.InvalidSupplyQuantityException;
import br.com.fiap.postech.domain.catalogservices.exception.NoMatchingCatalogServiceException;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import br.com.fiap.postech.domain.supply.model.Supply;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;

public class CatalogServicesUseCase {
    private final CatalogServicesPersistencePort persistencePort;
    private final SupplyPersistencePort supplyPersistencePort;

    public CatalogServicesUseCase(CatalogServicesPersistencePort persistencePort, SupplyPersistencePort supplyPersistencePort) {
        this.persistencePort = persistencePort;
        this.supplyPersistencePort = supplyPersistencePort;
    }

    public ScrollPage<CatalogServices> scroll(Long id, String name, Integer pageSize, String cursor) {
        final var result = persistencePort.scroll(id, name, pageSize, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingCatalogServiceException(name);
        }

        return result;
    }

    public CatalogServices getById(Long id){
        return persistencePort.findById(id).orElseThrow(() -> new CatalogServiceNotFoundException(id));
    }

    public CatalogServices create(CatalogServices catalogServices) {
        validate(catalogServices);
        persistencePort.findByName(catalogServices.getName()).ifPresent(s -> {
            throw new DuplicatedCatalogServiceException(catalogServices.getName());
        });
        return persistencePort.save(catalogServices);
    }

    public void delete(Long id) {
        if (!persistencePort.existsById(id)) {
            throw new CatalogServiceNotFoundException(id);
        }

        persistencePort.deleteById(id);
    }

    public CatalogServices update(Long id, CatalogServices catalogServices) {
        validate(catalogServices);
        CatalogServices existing = persistencePort.findById(id)
                .orElseThrow(() -> new CatalogServiceNotFoundException(id));
        catalogServices.setId(existing.getId());
        return persistencePort.save(catalogServices);
    }

    private void validate(CatalogServices catalogServices) {
        if (catalogServices.getName() == null || catalogServices.getName().isBlank()) {
            throw new InvalidCatalogServiceNameException();
        }
        if (catalogServices.getBasePrice() == null || catalogServices.getBasePrice().signum() <= 0) {
            throw new InvalidCatalogServicePriceException();
        }
        if (catalogServices.getSupplies() != null) {
            for (NeededSupplyEntity supply : catalogServices.getSupplies()) {
                Supply referencedSupply = supply.getSupply();
                if (referencedSupply != null && referencedSupply.getId() != null) {
                    supplyPersistencePort.findById(referencedSupply.getId())
                            .orElseThrow(() -> new CatalogServiceNotFoundException(referencedSupply.getId()));
                }
                if (supply.getSupplyAmount() == null || supply.getSupplyAmount() <= 0) {
                    throw new InvalidSupplyQuantityException();
                }
            }

        }
    }
}
