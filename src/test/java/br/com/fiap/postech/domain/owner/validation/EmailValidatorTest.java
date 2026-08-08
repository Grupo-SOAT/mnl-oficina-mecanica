package br.com.fiap.postech.domain.owner.validation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailValidatorTest {

    @Test
    void should_return_true_for_valid_email() {
        boolean result = EmailValidator.isValid(
                "teste@email.com"
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_for_valid_email_with_plus_and_underscore() {
        boolean result = EmailValidator.isValid(
                "teste_123+dev@email.com"
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_for_null_email() {
        boolean result = EmailValidator.isValid(null);

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_blank_email() {
        boolean result = EmailValidator.isValid("   ");

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_email_without_at_symbol() {
        boolean result = EmailValidator.isValid(
                "testeemail.com"
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_email_without_domain() {
        boolean result = EmailValidator.isValid(
                "teste@"
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_email_without_name() {
        boolean result = EmailValidator.isValid(
                "@email.com"
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_email_with_invalid_characters() {
        boolean result = EmailValidator.isValid(
                "teste#email.com"
        );

        assertThat(result).isFalse();
    }
}
