package br.com.fiap.postech.domain.user.exception;

public class SameUsernameException extends RuntimeException {
    public SameUsernameException() {
        super("Username already exists");
    }
}
