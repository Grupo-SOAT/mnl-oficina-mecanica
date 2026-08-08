package br.com.fiap.postech.domain.owner.exception;

public class InvalidEmailException extends RuntimeException{

    public InvalidEmailException(String email) {
        super("Invalid email: " + email);
    }
}
