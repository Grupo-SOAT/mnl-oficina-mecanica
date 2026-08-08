package br.com.fiap.postech.domain.serviceorder.exception;

import br.com.fiap.postech.domain.serviceorder.exception.reason.ServiceOrderExceptionReason;

public class ServiceOrderVehicleDataAbsentException extends RuntimeException {
    public final ServiceOrderExceptionReason reason = ServiceOrderExceptionReason.SERVICE_ORDER_VEHICLE_DATA_ABSENT;

    public ServiceOrderVehicleDataAbsentException() {
        super("You need to inform either an existing vehicleId or the data to create new one");
    }
}
