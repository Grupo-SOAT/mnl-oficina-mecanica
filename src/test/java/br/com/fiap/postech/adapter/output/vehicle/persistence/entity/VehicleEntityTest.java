package br.com.fiap.postech.adapter.output.vehicle.persistence.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.fiap.postech.domain.vehicle.model.Vehicle;

public class VehicleEntityTest {
    

    @Test
    void should_build_entity_with_default_id() {
        VehicleEntity entity = VehicleEntity.builder()
                .ownerId(1L)
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto")
                .build();

        assertThat(entity.getId()).isEqualTo(0L);
        assertThat(entity).isInstanceOf(Vehicle.class);

        assertThat(entity.getOwnerId()).isEqualTo(1L);
        assertThat(entity.getLicensePlate()).isEqualTo("ABC1234");
        assertThat(entity.getBrand()).isEqualTo("Toyota");
        assertThat(entity.getModel()).isEqualTo("Corolla");
        assertThat(entity.getYear()).isEqualTo(2022);
        assertThat(entity.getColor()).isEqualTo("Preto");
    }

    @ParameterizedTest
    @CsvSource({
            "1, ABC1234, Toyota, Corolla, 2022, Preto",
            "2, XYZ9999, Honda, Civic, 2023, Branco",
            "3, DEF5678, Ford, Focus, 2021, Azul"
    })
    void should_allow_mutation_via_lombok_setters(
            Long ownerId,
            String licensePlate,
            String brand,
            String model,
            Integer year,
            String color
    ) {
        VehicleEntity entity = new VehicleEntity();

        entity.setId(10L);
        entity.setOwnerId(ownerId);
        entity.setLicensePlate(licensePlate);
        entity.setBrand(brand);
        entity.setModel(model);
        entity.setYear(year);
        entity.setColor(color);

        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getOwnerId()).isEqualTo(ownerId);
        assertThat(entity.getLicensePlate()).isEqualTo(licensePlate);
        assertThat(entity.getBrand()).isEqualTo(brand);
        assertThat(entity.getModel()).isEqualTo(model);
        assertThat(entity.getYear()).isEqualTo(year);
        assertThat(entity.getColor()).isEqualTo(color);
    }
}
