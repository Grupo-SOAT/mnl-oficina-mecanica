package br.com.fiap.postech.adapter.output.owner.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import br.com.fiap.postech.domain.owner.model.Owner;

public class OwnerEntityTest {

    @Test
    void should_build_entity_with_default_id() {
        OwnerEntity entity = OwnerEntity.builder()
                .name("Name")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("1231432")
                .email("teste@email.com")
                .build();

        assertThat(entity.getId()).isEqualTo(0L);
        assertThat(entity).isInstanceOf(Owner.class);
    }
    
}
