package br.com.fiap.postech.domain.vehicle.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import br.com.fiap.postech.domain.vehicle.excecption.DuplicatedVehicleException;
import br.com.fiap.postech.domain.vehicle.excecption.InvalidLicensePlateException;
import br.com.fiap.postech.domain.vehicle.excecption.NoMatchingVehiclesException;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleNotFoundException;
import br.com.fiap.postech.domain.vehicle.excecption.reason.VehicleExceptionReason;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

public class VehicleExceptionTest {

    @ParameterizedTest
    @MethodSource("exceptionCases")
    void should_build_expected_exception_message(
            RuntimeException exception,
            String expectedMessage
    ) {
        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("reasonCases")
    void should_have_expected_reason(
            RuntimeException exception,
            VehicleExceptionReason expectedReason
    ) {
        VehicleExceptionReason actualReason = switch (exception) {
            case DuplicatedVehicleException ex -> ex.reason;
            case VehicleNotFoundException ex -> ex.reason;
            default -> null;
        };

        assertThat(actualReason).isEqualTo(expectedReason);
    }

    static Stream<Arguments> exceptionCases() {
        return Stream.of(
                arguments(
                        new VehicleNotFoundException(10L),
                        "Vehicle not found for id: 10"
                ),
                arguments(
                        new DuplicatedVehicleException("ABC1234"),
                        "Vehicle already exists for license plate: ABC1234"
                ),
                arguments(
                        new InvalidLicensePlateException("INVALID"),
                        "Invalid license plate: INVALID"
                ),
                arguments(
                        new NoMatchingVehiclesException("XYZ9999"),
                        "No matching vehicles for license plate: XYZ9999"
                )
        );
    }

    static Stream<Arguments> reasonCases() {
        return Stream.of(
                arguments(
                        new DuplicatedVehicleException("ABC1234"),
                        VehicleExceptionReason.VEHICLE_CONFLICT_DUPLICATED_LICENSE_PLATE
                ),
                arguments(
                        new VehicleNotFoundException(1L),
                        VehicleExceptionReason.VEHICLE_NOT_FOUND
                )
        );
    }
    
}
