package br.com.fiap.postech.domain.vehicle.excecption;

import br.com.fiap.postech.domain.vehicle.excecption.reason.VehicleExceptionReason;

public class VehicleOwnerDataAbsentException extends RuntimeException{
    public VehicleExceptionReason reason = VehicleExceptionReason.VEHICLE_OWNER_DATA_ABSENT;
    public VehicleOwnerDataAbsentException() {
        super("You need to inform either an existing ownerId or the data to create new one");
    }
}
