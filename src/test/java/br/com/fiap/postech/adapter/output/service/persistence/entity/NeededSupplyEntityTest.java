package br.com.fiap.postech.adapter.output.service.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NeededSupplyEntityTest {

    @Test
    void should_build_with_all_fields() {
        NeededSupplyEntity entity = NeededSupplyEntity.builder()
                .idSupply(1L)
                .note("check stock")
                .quantity(2)
                .build();

        assertThat(entity.getIdSupply()).isEqualTo(1L);
        assertThat(entity.getNote()).isEqualTo("check stock");
        assertThat(entity.getQuantity()).isEqualTo(2);
    }

    @Test
    void should_support_no_args_constructor_and_setters() {
        NeededSupplyEntity entity = new NeededSupplyEntity();
        entity.setIdSupply(2L);
        entity.setNote(null);
        entity.setQuantity(5);

        assertThat(entity.getIdSupply()).isEqualTo(2L);
        assertThat(entity.getNote()).isNull();
        assertThat(entity.getQuantity()).isEqualTo(5);
    }

    @Test
    void should_support_all_args_constructor() {
        NeededSupplyEntity entity = new NeededSupplyEntity(3L, "urgent", 10);

        assertThat(entity.getIdSupply()).isEqualTo(3L);
        assertThat(entity.getNote()).isEqualTo("urgent");
        assertThat(entity.getQuantity()).isEqualTo(10);
    }

    @Test
    void should_allow_null_note() {
        NeededSupplyEntity entity = NeededSupplyEntity.builder()
                .idSupply(4L)
                .note(null)
                .quantity(1)
                .build();

        assertThat(entity.getNote()).isNull();
    }
}
