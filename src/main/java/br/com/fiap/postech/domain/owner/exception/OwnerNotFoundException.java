package br.com.fiap.postech.domain.owner.exception;

import br.com.fiap.postech.domain.owner.exception.reason.OwnerExceptionReason;

public class OwnerNotFoundException extends RuntimeException{
    public OwnerExceptionReason reason = OwnerExceptionReason.OWNER_NOT_FOUND;
    public OwnerNotFoundException(Long id) {
        super("Owner not found for id: " + id);
    }
}
