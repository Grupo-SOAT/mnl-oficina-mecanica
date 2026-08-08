package br.com.fiap.postech.domain.owner.exception;

import br.com.fiap.postech.domain.owner.exception.reason.OwnerExceptionReason;

public class DuplicatedOwnerException extends RuntimeException{
    public OwnerExceptionReason reason = OwnerExceptionReason.OWNER_CONFLICT_DUPLICATED_DOCUMENT;
    public DuplicatedOwnerException(String document) {
        super("Owner already exists for document: " + document);
    }
}
