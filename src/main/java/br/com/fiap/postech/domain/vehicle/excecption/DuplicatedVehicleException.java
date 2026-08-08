package br.com.fiap.postech.domain.vehicle.excecption;

import br.com.fiap.postech.domain.vehicle.excecption.reason.VehicleExceptionReason;

public class DuplicatedVehicleException extends RuntimeException{
    public VehicleExceptionReason reason = VehicleExceptionReason.VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE;
    public DuplicatedVehicleException(String licensePlate) {
        super("Vehicle already exists for license plate: " + licensePlate);
    }
}
