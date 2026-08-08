package br.com.fiap.postech.domain.owner.exception;

public class NoMatchingOwnersException extends RuntimeException{
    public NoMatchingOwnersException(String email) {
        super("No matching owners for email: " + email);
    }
    
}
