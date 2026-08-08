package br.com.fiap.postech.domain.vehicle.model;

import br.com.fiap.postech.domain.owner.model.Owner;

public record VehicleCascadeCreationCommand(
        Owner owner,
        Vehicle vehicle
) {
}
