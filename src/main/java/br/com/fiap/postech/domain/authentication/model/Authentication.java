package br.com.fiap.postech.domain.authentication.model;

import java.time.OffsetDateTime;

public record Authentication(String token, OffsetDateTime offSetDateTime ) {
    
}
