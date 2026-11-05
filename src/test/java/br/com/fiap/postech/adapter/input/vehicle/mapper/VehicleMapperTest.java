package br.com.fiap.postech.adapter.input.vehicle.mapper;

import br.com.fiap.postech.adapter.input.api.model.*;
import org.junit.jupiter.api.Test;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.vehicle.persistence.entity.VehicleEntity;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

public class VehicleMapperTest {

    @Test
    void should_map_from_api_request_to_vehicle() {
        VehicleRequest request = new VehicleRequest()
                .ownerId(1L)
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto");

        Vehicle result = VehicleMapper.fromApiRequest(request);

        assertThat(result).isInstanceOf(VehicleEntity.class);
        assertThat(result.getOwnerId()).isEqualTo(1L);
        assertThat(result.getLicensePlate()).isEqualTo("ABC1234");
        assertThat(result.getBrand()).isEqualTo("Toyota");
        assertThat(result.getModel()).isEqualTo("Corolla");
        assertThat(result.getYear()).isEqualTo(2022);
        assertThat(result.getColor()).isEqualTo("Preto");
    }

    @Test
    void should_map_from_api_data_to_vehicle() {
        VehicleData data = new VehicleData()
                .id(10L)
                .ownerId(2L)
                .licensePlate("XYZ9999")
                .brand("Honda")
                .model("Civic")
                .year(2023)
                .color("Branco");

        Vehicle result = VehicleMapper.fromApiData(data);

        assertThat(result).isInstanceOf(VehicleEntity.class);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getOwnerId()).isEqualTo(2L);
        assertThat(result.getLicensePlate()).isEqualTo("XYZ9999");
        assertThat(result.getBrand()).isEqualTo("Honda");
        assertThat(result.getModel()).isEqualTo("Civic");
        assertThat(result.getYear()).isEqualTo(2023);
        assertThat(result.getColor()).isEqualTo("Branco");
    }

    @Test
    void should_map_vehicle_to_api_data() {
        Vehicle vehicle = VehicleEntity.builder()
                .id(20L)
                .ownerId(3L)
                .licensePlate("DEF5678")
                .brand("Ford")
                .model("Focus")
                .year(2021)
                .color("Azul")
                .build();

        VehicleData result = VehicleMapper.toApiData(vehicle);

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getOwnerId()).isEqualTo(3L);
        assertThat(result.getLicensePlate()).isEqualTo("DEF5678");
        assertThat(result.getBrand()).isEqualTo("Ford");
        assertThat(result.getModel()).isEqualTo("Focus");
        assertThat(result.getYear()).isEqualTo(2021);
        assertThat(result.getColor()).isEqualTo("Azul");
    }

    @Test
    void should_map_scroll_page_to_paginated_response() {
        Vehicle vehicle1 = VehicleEntity.builder()
                .id(1L)
                .ownerId(1L)
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto")
                .build();

        Vehicle vehicle2 = VehicleEntity.builder()
                .id(2L)
                .ownerId(2L)
                .licensePlate("XYZ9999")
                .brand("Honda")
                .model("Civic")
                .year(2023)
                .color("Branco")
                .build();

        ScrollPage<Vehicle> page = ScrollPage.<Vehicle>builder()
                .data(List.of(vehicle1, vehicle2))
                .cursor("2")
                .pageSize(10)
                .isLast(false)
                .build();

        PaginatedVehicleResponse result =
                VehicleMapper.toPaginatedResponse(page);

        assertThat(result.getCursor()).isEqualTo("2");
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getIsLast()).isFalse();

        assertThat(result.getData()).hasSize(2);

        assertThat(result.getData().get(0).getId()).isEqualTo(1L);
        assertThat(result.getData().get(0).getLicensePlate()).isEqualTo("ABC1234");

        assertThat(result.getData().get(1).getId()).isEqualTo(2L);
        assertThat(result.getData().get(1).getLicensePlate()).isEqualTo("XYZ9999");
    }
}
