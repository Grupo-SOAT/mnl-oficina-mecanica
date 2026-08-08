package br.com.fiap.postech.domain.owner.validation;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;

public class DocumentValidator {
    
    public static boolean isValid(String document, DocumentType type) {

        String normalized = document.replaceAll("\\D", "");

        return switch (type) {
            case CPF -> isValidCPF(normalized);
            case CNPJ -> isValidCNPJ(normalized);
        };
    }

    private static boolean isValidCPF(String cpf) {

        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        int sum = 0;

        for (int i = 0; i < 9; i++) {
            sum += (cpf.charAt(i) - '0') * (10 - i);
        }

        int firstDigit = 11 - (sum % 11);

        if (firstDigit >= 10) {
            firstDigit = 0;
        }

        if (firstDigit != (cpf.charAt(9) - '0')) {
            return false;
        }

        sum = 0;

        for (int i = 0; i < 10; i++) {
            sum += (cpf.charAt(i) - '0') * (11 - i);
        }

        int secondDigit = 11 - (sum % 11);

        if (secondDigit >= 10) {
            secondDigit = 0;
        }

        return secondDigit == (cpf.charAt(10) - '0');
    }

    private static boolean isValidCNPJ(String cnpj) {

        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }

        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        int[] firstWeights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] secondWeights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;

        for (int i = 0; i < 12; i++) {
            sum += (cnpj.charAt(i) - '0') * firstWeights[i];
        }

        int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        if (firstDigit != (cnpj.charAt(12) - '0')) {
            return false;
        }

        sum = 0;

        for (int i = 0; i < 13; i++) {
            sum += (cnpj.charAt(i) - '0') * secondWeights[i];
        }

        int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

        return secondDigit == (cnpj.charAt(13) - '0');
    }
}
