package br.com.fiap.postech.adapter.output.catalogservice.persistence.entity;

import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogServicesEntityTest {
    @Test
    void should_build_entity_with_default_id() {
        List<NeededSupplyEntity> neededSupplyEntities = new ArrayList<>();
        neededSupplyEntities.add(NeededSupplyEntity.builder()
                .servicesSuppliesId(1L)
                .supplyAmount(100)
                .supply(SupplyEntity.builder()
                        .id(1L)
                        .name("Default")
                        .unitPrice(BigDecimal.valueOf(1080))
                        .build()).
                build());

        CatalogServicesEntity entity = CatalogServicesEntity.builder()
                .name("Nome")
                .description("Descricao")
                .basePrice(new BigDecimal("100.00"))
                .supplies(neededSupplyEntities)
                .build();

        assertThat(entity.getId()).isEqualTo(0L);
        assertThat(entity).isInstanceOf(CatalogServicesEntity.class);
    }
}
