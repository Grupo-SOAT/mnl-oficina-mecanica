package br.com.fiap.postech.domain.authentication.model;

public record UserChangePassword(String newPassword, String username, String password) {
    
}
