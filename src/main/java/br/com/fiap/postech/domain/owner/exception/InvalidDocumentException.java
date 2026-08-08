package br.com.fiap.postech.domain.owner.exception;

public class InvalidDocumentException extends RuntimeException{

    public InvalidDocumentException(String document) {
        super("Invalid document: " + document);
    }
    
}
