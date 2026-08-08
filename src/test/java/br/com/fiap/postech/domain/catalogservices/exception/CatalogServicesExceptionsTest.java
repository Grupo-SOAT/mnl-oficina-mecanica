package br.com.fiap.postech.domain.catalogservices.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CatalogServicesExceptionsTest {
    @ParameterizedTest
    @MethodSource("exceptionCases")
    void should_build_expected_exception_message(RuntimeException exception, String expectedMessage) {
        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedMessage);
    }

    static Stream<Arguments> exceptionCases() {
        return Stream.of(
                arguments(new CatalogServiceNotFoundException(10L), "Catalog services not found for id: 10"),
                arguments(new DuplicatedCatalogServiceException("Pintura"), "Catalog services already exists for name: Pintura"),
                arguments(new NoMatchingCatalogServiceException("Troca de oleo"), "No matching catalog service for name: Troca de oleo")
        );
    }
}
