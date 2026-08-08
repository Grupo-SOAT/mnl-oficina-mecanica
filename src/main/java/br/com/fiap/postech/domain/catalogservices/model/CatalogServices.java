package br.com.fiap.postech.domain.catalogservices.model;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;

import java.math.BigDecimal;
import java.util.List;

public interface CatalogServices {
    Long getId();

    void setId(Long catalogServiceId);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    BigDecimal getBasePrice();

    void setBasePrice(BigDecimal basePrice);

    List<NeededSupplyEntity> getSupplies();

    void setSupplies(List<NeededSupplyEntity> neededSupplies);
}
