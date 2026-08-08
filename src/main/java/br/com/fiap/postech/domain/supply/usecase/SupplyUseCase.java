package br.com.fiap.postech.domain.supply.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.supply.exception.DuplicatedSupplyException;
import br.com.fiap.postech.domain.supply.exception.NoMatchingSuppliesException;
import br.com.fiap.postech.domain.supply.exception.SupplyNotFoundException;
import br.com.fiap.postech.domain.supply.model.Supply;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;

public class SupplyUseCase {
    private final SupplyPersistencePort persistencePort;

    public SupplyUseCase(SupplyPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    public ScrollPage<Supply> scroll(String sku, Integer pageSize, String cursor) {
        final var result = persistencePort.scroll(sku, pageSize, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingSuppliesException(sku);
        }

        return result;
    }

    public Supply getById(Long id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new SupplyNotFoundException(id));
    }

    public Supply create(Supply supply) {
        persistencePort.findBySku(supply.getSku()).ifPresent(s -> {
            throw new DuplicatedSupplyException(supply.getSku());
        });

        if (supply.getReservedQuantity() == null) {
            supply.setReservedQuantity(0);
        }
        if (supply.getAvailableQuantity() == null) {
            supply.setAvailableQuantity(0);
        }

        return persistencePort.save(supply);
    }

    public Supply update(Long id, Supply supply) {
        persistencePort.findBySku(supply.getSku()).ifPresent(s -> {
            throw new DuplicatedSupplyException(supply.getSku());
        });

        final var existing = persistencePort.findById(id)
                .orElseThrow(() -> new SupplyNotFoundException(id));
        supply.setId(id);
        supply.setCreatedAt(existing.getCreatedAt());

        return persistencePort.save(supply);
    }

    public void delete(Long id) {
        if (!persistencePort.existsById(id)) {
            throw new SupplyNotFoundException(id);
        }

        persistencePort.deleteById(id);
    }
}
