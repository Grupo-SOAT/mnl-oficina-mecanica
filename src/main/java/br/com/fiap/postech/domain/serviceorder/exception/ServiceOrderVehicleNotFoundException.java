package br.com.fiap.postech.domain.serviceorder.exception;

import br.com.fiap.postech.domain.serviceorder.exception.reason.ServiceOrderExceptionReason;

public class ServiceOrderVehicleNotFoundException extends RuntimeException {
    public final ServiceOrderExceptionReason reason = ServiceOrderExceptionReason.SERVICE_ORDER_VEHICLE_NOT_FOUND;

    public ServiceOrderVehicleNotFoundException(Long vehicleId) {
        super("Vehicle not found for id: " + vehicleId);
    }
}
