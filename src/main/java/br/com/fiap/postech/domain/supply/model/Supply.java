package br.com.fiap.postech.domain.supply.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface Supply {
    Long getId();

    void setId(Long id);

    String getSku();

    void setSku(String sku);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    BigDecimal getUnitPrice();

    void setUnitPrice(BigDecimal unitPrice);

    Long getSuppliedBy();

    void setSuppliedBy(Long suppliedBy);

    Integer getReservedQuantity();

    void setReservedQuantity(Integer reservedQuantity);

    Integer getAvailableQuantity();

    void setAvailableQuantity(Integer availableQuantity);

    LocalDateTime getCreatedAt();

    void setCreatedAt(LocalDateTime createdAt);
}
