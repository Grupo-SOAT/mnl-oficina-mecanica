package br.com.fiap.postech.domain.vehicle.excecption;

public class NoMatchingVehiclesException extends RuntimeException{
    public NoMatchingVehiclesException(String licensePlate) {
        super("No matching vehicles for license plate: " + licensePlate);
    }
}
