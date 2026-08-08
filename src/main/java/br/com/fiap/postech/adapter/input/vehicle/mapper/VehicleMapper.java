package br.com.fiap.postech.adapter.input.vehicle.mapper;

import br.com.fiap.postech.adapter.input.api.model.PaginatedVehicleResponse;
import br.com.fiap.postech.adapter.input.api.model.VehicleCascadeRequest;
import br.com.fiap.postech.adapter.input.api.model.VehicleData;
import br.com.fiap.postech.adapter.input.api.model.VehicleRequest;
import br.com.fiap.postech.adapter.input.owner.mapper.OwnerMapper;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.vehicle.persistence.entity.VehicleEntity;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;

import br.com.fiap.postech.domain.vehicle.model.VehicleCascadeCreationCommand;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

public class VehicleMapper {

    public static Vehicle fromApiRequest(@NonNull VehicleRequest request) {
        return VehicleEntity.builder()
                .ownerId(request.getOwnerId())
                .licensePlate(request.getLicensePlate())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .build();
    }

    public static VehicleCascadeCreationCommand buildCascadeCreationCommand(@NotNull VehicleCascadeRequest request) {
        final var vehicle = VehicleEntity.builder()
                .ownerId(request.getOwnerId())
                .licensePlate(request.getLicensePlate())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .color(request.getColor())
                .build();
        final var owner = (request.getOwner() != null)
                ? OwnerMapper.fromApiRequest(request.getOwner())
                : null;

        return new VehicleCascadeCreationCommand(owner, vehicle);
    }

    public static Vehicle fromApiData(@NonNull VehicleData data) {
        return VehicleEntity.builder()
                .id(data.getId())
                .ownerId(data.getOwnerId())
                .licensePlate(data.getLicensePlate())
                .brand(data.getBrand())
                .model(data.getModel())
                .year(data.getYear())
                .color(data.getColor())
                .build();
    }

    public static VehicleData toApiData(@NonNull Vehicle vehicle) {
        return new VehicleData()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwnerId())
                .licensePlate(vehicle.getLicensePlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .color(vehicle.getColor());
    }

    public static PaginatedVehicleResponse toPaginatedResponse(@NonNull ScrollPage<Vehicle> page) {
        final var result = new PaginatedVehicleResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(VehicleMapper.toApiData(item)));

        return result;
    }
}
