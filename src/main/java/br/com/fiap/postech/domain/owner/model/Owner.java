package br.com.fiap.postech.domain.owner.model;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import java.time.LocalDateTime;

public interface Owner {
    Long getId();

    void setId(Long id);

    String getName();

    void setName(String name);

    String getDocument();

    void setDocument(String document);

    DocumentType getDocumentType();

    void setDocumentType(DocumentType documentType);

    String getPhone();

    void setPhone(String phone);
    
    String getEmail();

    void setEmail(String email);

    LocalDateTime getCreatedAt();

    void setCreatedAt(LocalDateTime createdAt);
}
