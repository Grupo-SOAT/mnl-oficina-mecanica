package br.com.fiap.postech.domain.owner.validation;

import org.junit.jupiter.api.Test;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentValidatorTest {
    @Test
    void should_return_true_for_valid_cpf() {
        boolean result = DocumentValidator.isValid(
                "31058167049",
                DocumentType.CPF
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_for_invalid_cpf() {
        boolean result = DocumentValidator.isValid(
                "12345678900",
                DocumentType.CPF
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_cpf_with_repeated_digits() {
        boolean result = DocumentValidator.isValid(
                "11111111111",
                DocumentType.CPF
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_cpf_with_invalid_length() {
        boolean result = DocumentValidator.isValid(
                "1234567890",
                DocumentType.CPF
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_for_valid_cnpj() {
        boolean result = DocumentValidator.isValid(
                "11222333000181",
                DocumentType.CNPJ
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_for_invalid_cnpj() {
        boolean result = DocumentValidator.isValid(
                "11222333000100",
                DocumentType.CNPJ
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_cnpj_with_repeated_digits() {
        boolean result = DocumentValidator.isValid(
                "11111111111111",
                DocumentType.CNPJ
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_false_for_cnpj_with_invalid_length() {
        boolean result = DocumentValidator.isValid(
                "123456789",
                DocumentType.CNPJ
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_validate_cpf_with_mask() {
        boolean result = DocumentValidator.isValid(
                "310.581.670-49",
                DocumentType.CPF
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_validate_cnpj_with_mask() {
        boolean result = DocumentValidator.isValid(
                "11.222.333/0001-81",
                DocumentType.CNPJ
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_when_cpf_first_digit_is_invalid() {
        boolean result = DocumentValidator.isValid(
                "31058167039",
                DocumentType.CPF
        );

        assertThat(result).isFalse();
    }

    @Test
    void should_return_true_when_cpf_first_digit_results_in_zero() {
        boolean result = DocumentValidator.isValid(
                "12345678909",
                DocumentType.CPF
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_when_cpf_second_digit_results_in_zero() {
        boolean result = DocumentValidator.isValid(
                "12345678909",
                DocumentType.CPF
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_when_cnpj_first_digit_results_in_zero() {
        boolean result = DocumentValidator.isValid(
                "11444777000161",
                DocumentType.CNPJ
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_true_when_cnpj_second_digit_results_in_zero() {
        boolean result = DocumentValidator.isValid(
                "11444777000161",
                DocumentType.CNPJ
        );

        assertThat(result).isTrue();
    }

    @Test
    void should_return_false_when_cnpj_second_digit_is_invalid() {
        boolean result = DocumentValidator.isValid(
                "11444777000162",
                DocumentType.CNPJ
        );

        assertThat(result).isFalse();
    }
}
