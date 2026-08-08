package br.com.fiap.postech.domain.supply.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class SupplyExceptionsTest {
    @ParameterizedTest
    @MethodSource("exceptionCases")
    void should_build_expected_exception_message(RuntimeException exception, String expectedMessage) {
        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedMessage);
    }

    static Stream<Arguments> exceptionCases() {
        return Stream.of(
                arguments(new SupplyNotFoundException(10L), "Supply not found for id: 10"),
                arguments(new DuplicatedSupplyException("SKU-1"), "Supply already exists for sku: SKU-1"),
                arguments(new NoMatchingSuppliesException("SKU-X"), "No matching supplies for sku: SKU-X")
        );
    }
}
