package br.com.fiap.postech.adapter.output.service.persistence.entity;

import br.com.fiap.postech.domain.service.model.NeededSupply;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceEntityTest {

    @Test
    void should_convert_needed_supplies_to_domain_model() {
        ServiceEntity entity = ServiceEntity.builder()
                .id(1L)
                .serviceOrderId(10L)
                .catalogServiceId(5L)
                .price(new BigDecimal("100.00"))
                .status("AWAITING_APPROVAL")
                .build();

        entity.setNeededSupplies(List.of(
                NeededSupply.builder().idSupply(1L).note("urgent").quantity(2).build(),
                NeededSupply.builder().idSupply(2L).note(null).quantity(1).build()
        ));

        List<NeededSupply> supplies = entity.getNeededSupplies();

        assertThat(supplies).hasSize(2);
        assertThat(supplies.get(0).getIdSupply()).isEqualTo(1L);
        assertThat(supplies.get(0).getNote()).isEqualTo("urgent");
        assertThat(supplies.get(0).getQuantity()).isEqualTo(2);
        assertThat(supplies.get(1).getIdSupply()).isEqualTo(2L);
        assertThat(supplies.get(1).getNote()).isNull();
    }

    @Test
    void should_handle_null_needed_supplies() {
        ServiceEntity entity = ServiceEntity.builder().id(1L).build();

        entity.setNeededSupplies(null);

        assertThat(entity.getNeededSupplies()).isEmpty();
    }

    @Test
    void should_return_id_as_string_from_to_string() {
        ServiceEntity entity = ServiceEntity.builder().id(42L).build();

        assertThat(entity.toString()).isEqualTo("42");
    }

    @Test
    void should_build_with_default_empty_supplies() {
        ServiceEntity entity = ServiceEntity.builder().id(1L).build();

        assertThat(entity.getNeededSupplies()).isEmpty();
    }
}
