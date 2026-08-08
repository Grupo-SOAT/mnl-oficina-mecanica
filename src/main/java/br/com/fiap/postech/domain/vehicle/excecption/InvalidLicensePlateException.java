package br.com.fiap.postech.domain.vehicle.excecption;

public class InvalidLicensePlateException extends RuntimeException {

    public InvalidLicensePlateException(String licensePlate) {
        super("Invalid license plate: " + licensePlate);
    }
}
