package br.com.fiap.postech.domain.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found for id: " + id);
    }

    public UserNotFoundException() {
        super("User not found");
    }
}
