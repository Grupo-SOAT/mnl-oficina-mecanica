package br.com.fiap.postech.domain.user.exception;

public class UsernameNotFoundException extends RuntimeException {
    public UsernameNotFoundException(String username) {
        super("User not found by username: " + username);
    }
}
