package br.com.fiap.postech.adapter.input.vehicle.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.fiap.postech.adapter.input.api.model.PaginatedVehicleResponse;
import br.com.fiap.postech.adapter.input.api.model.VehicleData;
import br.com.fiap.postech.adapter.input.api.model.VehicleRequest;
import br.com.fiap.postech.adapter.input.vehicle.mapper.VehicleMapper;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.domain.vehicle.usecase.VehicleUseCase;

@ExtendWith(MockitoExtension.class)
public class VehicleControllerTest {

    @Mock
    private VehicleUseCase vehicleUseCase;

    @InjectMocks
    private VehicleController controller;

    @Test
    void should_create_vehicle_and_return_created() {
        VehicleRequest request = new VehicleRequest()
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto")
                .ownerId(1L);

        Vehicle vehicle = mock(Vehicle.class);
        Vehicle created = mock(Vehicle.class);

        VehicleData responseData = new VehicleData()
                .id(1L)
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto")
                .ownerId(1L);

        try (MockedStatic<VehicleMapper> mapper = mockStatic(VehicleMapper.class)) {

            mapper.when(() -> VehicleMapper.fromApiRequest(request))
                    .thenReturn(vehicle);

            when(vehicleUseCase.create(vehicle))
                    .thenReturn(created);

            mapper.when(() -> VehicleMapper.toApiData(created))
                    .thenReturn(responseData);

            ResponseEntity<VehicleData> response = controller.createVehicle(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(responseData);

            verify(vehicleUseCase).create(vehicle);
        }
    }

    @Test
    void should_delete_vehicle_and_return_accepted() {
        ResponseEntity<Void> response = controller.deleteVehicle(1L);

        verify(vehicleUseCase).delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void should_get_vehicle_by_id_and_return_ok() {
        Vehicle vehicle = mock(Vehicle.class);

        VehicleData responseData = new VehicleData()
                .id(1L)
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto")
                .ownerId(1L);

        when(vehicleUseCase.getById(1L))
                .thenReturn(vehicle);

        try (MockedStatic<VehicleMapper> mapper = mockStatic(VehicleMapper.class)) {

            mapper.when(() -> VehicleMapper.toApiData(vehicle))
                    .thenReturn(responseData);

            ResponseEntity<VehicleData> response = controller.getVehicleById(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(responseData);

            verify(vehicleUseCase).getById(1L);
        }
    }

    @Test
    void should_list_vehicles_and_return_paginated_response() {
        Vehicle vehicle = mock(Vehicle.class);

        ScrollPage<Vehicle> pageResult = ScrollPage.<Vehicle>builder()
                .data(List.of(vehicle))
                .cursor("1")
                .pageSize(10)
                .isLast(false)
                .build();

        PaginatedVehicleResponse responseData = new PaginatedVehicleResponse();

        when(vehicleUseCase.scroll("ABC1234", 10, "5"))
                .thenReturn(pageResult);

        try (MockedStatic<VehicleMapper> mapper = mockStatic(VehicleMapper.class)) {

            mapper.when(() -> VehicleMapper.toPaginatedResponse(pageResult))
                    .thenReturn(responseData);

            ResponseEntity<PaginatedVehicleResponse> response =
                    controller.listVehicles("ABC1234", 10, "5");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(responseData);

            verify(vehicleUseCase).scroll("ABC1234", 10, "5");
        }
    }

    @Test
    void should_update_vehicle_and_return_ok() {
        VehicleData requestData = new VehicleData()
                .licensePlate("XYZ9999")
                .brand("Honda")
                .model("Civic")
                .year(2023)
                .color("Branco")
                .ownerId(2L);

        Vehicle existingVehicle = mock(Vehicle.class);
        Vehicle updatedVehicle = mock(Vehicle.class);

        VehicleData responseData = new VehicleData()
                .id(1L)
                .licensePlate("XYZ9999")
                .brand("Honda")
                .model("Civic")
                .year(2023)
                .color("Branco")
                .ownerId(2L);

        try (MockedStatic<VehicleMapper> mapper = mockStatic(VehicleMapper.class)) {

            mapper.when(() -> VehicleMapper.fromApiData(requestData))
                    .thenReturn(existingVehicle);

            when(vehicleUseCase.update(1L, existingVehicle))
                    .thenReturn(updatedVehicle);

            mapper.when(() -> VehicleMapper.toApiData(updatedVehicle))
                    .thenReturn(responseData);

            ResponseEntity<VehicleData> response =
                    controller.updateVehicle(1L, requestData);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(responseData);

            verify(vehicleUseCase).update(1L, existingVehicle);
        }
    }
    
}
