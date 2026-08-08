package br.com.fiap.postech.domain.vehicle.excecption;

import br.com.fiap.postech.domain.vehicle.excecption.reason.VehicleExceptionReason;

public class VehicleNotFoundException extends RuntimeException{
    public VehicleExceptionReason reason = VehicleExceptionReason.VEHICLE_NOT_FOUND;
    public VehicleNotFoundException(Long id) {
        super("Vehicle not found for id: " + id);
    }
}
