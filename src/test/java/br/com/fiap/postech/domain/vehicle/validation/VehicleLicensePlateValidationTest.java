package br.com.fiap.postech.domain.vehicle.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class VehicleLicensePlateValidationTest {

    @ParameterizedTest
    @CsvSource({
            "ABC1234",
            "abc1234",
            "ABC-1234",
            "ABC 1234",
            "BRA2E19",
            "bra2e19",
            "BRA-2E19",
            "BRA 2E19"
    })
    void should_return_true_for_valid_license_plates(String licensePlate) {
        boolean result = VehicleLicensePlateValidator.isValid(licensePlate);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "'', false",
            "'   ', false",
            "AB1234, false",
            "ABCD1234, false",
            "ABC123, false",
            "ABC12345, false",
            "123ABC4, false",
            "AAA1AA1, false",
            "A1B2C3D, false",
            "ABC@123, false"
    })
    void should_return_false_for_invalid_license_plates(
            String licensePlate,
            boolean expected
    ) {
        boolean result = VehicleLicensePlateValidator.isValid(licensePlate);

        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = "null", nullValues = "null")
    void should_return_false_when_license_plate_is_null(
            String licensePlate
    ) {
        boolean result = VehicleLicensePlateValidator.isValid(licensePlate);

        assertThat(result).isFalse();
    }
}
