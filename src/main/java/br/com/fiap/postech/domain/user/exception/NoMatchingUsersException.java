package br.com.fiap.postech.domain.user.exception;

public class NoMatchingUsersException extends RuntimeException {
    public NoMatchingUsersException(String username) {
        super("No matching users for usernames: " + username);
    }
}
