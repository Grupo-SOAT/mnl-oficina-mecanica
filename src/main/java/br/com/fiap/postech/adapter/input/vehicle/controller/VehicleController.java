package br.com.fiap.postech.adapter.input.vehicle.controller;

import br.com.fiap.postech.adapter.input.api.model.PaginatedVehicleResponse;
import br.com.fiap.postech.adapter.input.api.model.VehicleData;
import br.com.fiap.postech.adapter.input.api.model.VehicleRequest;
import br.com.fiap.postech.adapter.input.vehicle.mapper.VehicleMapper;
import br.com.fiap.postech.domain.vehicle.usecase.VehicleUseCase;
import br.com.fiap.postech.port.api.VehiclesApi;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VehicleController implements VehiclesApi {
    private final VehicleUseCase vehicleUseCase;
    
    @Override
    public ResponseEntity<VehicleData> createVehicle(VehicleRequest vehicleRequest) {
        final var newVehicle = VehicleMapper.fromApiRequest(vehicleRequest);
        final var created = vehicleUseCase.create(newVehicle);
        final var responseBody = VehicleMapper.toApiData(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Override
    public ResponseEntity<Void> deleteVehicle(Long id) {
        vehicleUseCase.delete(id);

        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<VehicleData> getVehicleById(Long id) {
        final var vehicle = vehicleUseCase.getById(id);
        final var responseBody = VehicleMapper.toApiData(vehicle);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<PaginatedVehicleResponse> listVehicles(String licensePlate, Integer pageSize, String cursor) {
        final var pageResult = vehicleUseCase.scroll(licensePlate, pageSize, cursor);
        final var responseBody = VehicleMapper.toPaginatedResponse(pageResult);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    public ResponseEntity<VehicleData> updateVehicle(Long id, VehicleData vehicleData) {
        final var existingVehicle = VehicleMapper.fromApiData(vehicleData);
        final var updated = vehicleUseCase.update(id, existingVehicle);
        final var responseBody = VehicleMapper.toApiData(updated);

        return ResponseEntity.ok(responseBody);
    }
}
