package br.com.fiap.postech.domain.vehicle.validation;

import java.util.regex.Pattern;

public class VehicleLicensePlateValidator {

    private static final Pattern LICENSE_PLATE_PATTERN =
    Pattern.compile(
            "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$"
    );

    public static boolean isValid(String licensePlate) {

        if (licensePlate == null || licensePlate.isBlank()) {
            return false;
        }

        String normalized = licensePlate
                .replace("-", "")
                .replace(" ", "")
                .toUpperCase();

        return LICENSE_PLATE_PATTERN.matcher(normalized).matches();
    }
    
}
