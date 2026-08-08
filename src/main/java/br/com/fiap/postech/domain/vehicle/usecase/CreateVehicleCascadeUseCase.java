package br.com.fiap.postech.domain.vehicle.usecase;

import br.com.fiap.postech.domain.owner.usecase.OwnerUseCase;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleOwnerDataAbsentException;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.domain.vehicle.model.VehicleCascadeCreationCommand;

public class CreateVehicleCascadeUseCase {

    private final OwnerUseCase ownerUseCase;
    private final VehicleUseCase vehicleUseCase;

    public CreateVehicleCascadeUseCase(OwnerUseCase ownerUseCase, VehicleUseCase vehicleUseCase) {
        this.ownerUseCase = ownerUseCase;
        this.vehicleUseCase = vehicleUseCase;
    }

    public Vehicle execute(VehicleCascadeCreationCommand command) {
        final var vehicle = command.vehicle();
        var owner = command.owner();

        if (vehicle == null) return null;

        if (vehicle.getOwnerId() == null && owner == null)
            throw new VehicleOwnerDataAbsentException();

        if (vehicle.getOwnerId() != null) return vehicleUseCase.create(vehicle);

        owner = ownerUseCase.create(owner);
        vehicle.setOwnerId(owner.getId());
        return vehicleUseCase.create(vehicle);
    }

}
