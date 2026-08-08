package br.com.fiap.postech.domain.serviceorder.model;

import br.com.fiap.postech.domain.vehicle.model.VehicleCascadeCreationCommand;

import java.util.List;

public record ServiceOrderCascadeCreationCommand(
        VehicleCascadeCreationCommand vehicleCascadeCreationCommand,
        ServiceOrder serviceOrder,
        List<Long> catalogServiceIds
) {
}
