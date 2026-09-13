package br.com.fiap.postech.config;

public class ClienteResourceAccessDeniedException extends RuntimeException {

    public ClienteResourceAccessDeniedException() {
        super("Cliente autenticado nao tem acesso a este recurso");
    }
}
