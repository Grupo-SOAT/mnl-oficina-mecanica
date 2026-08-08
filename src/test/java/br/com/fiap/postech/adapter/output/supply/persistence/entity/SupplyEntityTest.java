package br.com.fiap.postech.adapter.output.supply.persistence.entity;

import br.com.fiap.postech.domain.supply.model.Supply;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SupplyEntityTest {
    @Test
    void should_build_entity_with_default_id() {
        SupplyEntity entity = SupplyEntity.builder()
                .sku("SKU-1")
                .name("Nome")
                .description("Descricao")
                .unitPrice(new BigDecimal("10.00"))
                .build();

        assertThat(entity.getId()).isEqualTo(0L);
        assertThat(entity).isInstanceOf(Supply.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 10",
            "3, 7",
            "5, 0"
    })
    void should_allow_mutation_via_lombok_setters(Integer reserved, Integer available) {
        SupplyEntity entity = new SupplyEntity();

        entity.setReservedQuantity(reserved);
        entity.setAvailableQuantity(available);

        assertThat(entity.getReservedQuantity()).isEqualTo(reserved);
        assertThat(entity.getAvailableQuantity()).isEqualTo(available);
    }
}
