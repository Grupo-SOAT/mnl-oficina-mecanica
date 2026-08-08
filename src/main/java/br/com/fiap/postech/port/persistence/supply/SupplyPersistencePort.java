package br.com.fiap.postech.port.persistence.supply;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.supply.model.Supply;

import java.util.List;
import java.util.Optional;

public interface SupplyPersistencePort {
    ScrollPage<Supply> scroll(String sku, Integer pageSize, String cursor);

    Optional<Supply> findById(Long id);

    Optional<Supply> findBySku(String sku);

    List<Supply> findAllById(List<Long> ids);

    Supply save(Supply supply);

    void deleteById(Long id);

    boolean existsById(Long id);
}
