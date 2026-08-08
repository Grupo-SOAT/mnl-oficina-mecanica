package br.com.fiap.postech.domain.vehicle.excecption;

public class InvalidVehicleYearException extends RuntimeException {
    public InvalidVehicleYearException(Integer year) {
        super("Invalid vehicle year: " + year);
    }
}
