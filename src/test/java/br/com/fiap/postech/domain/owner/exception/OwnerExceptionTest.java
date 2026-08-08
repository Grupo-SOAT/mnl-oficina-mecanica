package br.com.fiap.postech.domain.owner.exception;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

public class OwnerExceptionTest {
    @ParameterizedTest
    @MethodSource("exceptionCases")
    void should_build_expected_exception_message(RuntimeException exception, String expectedMessage) {
        assertThat(exception)
                .isInstanceOf(RuntimeException.class)
                .hasMessage(expectedMessage);
    }

    static Stream<Arguments> exceptionCases() {
        return Stream.of(
                arguments(new OwnerNotFoundException(10L), "Owner not found for id: 10"),
                arguments(new DuplicatedOwnerException("31058167049"), "Owner already exists for document: 31058167049"),
                arguments(new NoMatchingOwnersException("teste@email.com"), "No matching owners for email: teste@email.com"),
                arguments(new InvalidDocumentException("31058167045"), "Invalid document: 31058167045"),
                arguments(new InvalidEmailException("teste2@email.com"), "Invalid email: teste2@email.com")
        );
    }
    
}
