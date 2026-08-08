package br.com.fiap.postech.domain.owner.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.exception.DuplicatedOwnerException;
import br.com.fiap.postech.domain.owner.exception.InvalidDocumentException;
import br.com.fiap.postech.domain.owner.exception.InvalidEmailException;
import br.com.fiap.postech.domain.owner.exception.NoMatchingOwnersException;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class OwnerUseCaseTest {
    @Mock
    private OwnerPersistencePort persistencePort;

    @InjectMocks
    private OwnerUseCase useCase;

    @Test
    void should_delegate_scroll_to_persistence() {
        ScrollPage<Owner> expected = ScrollPage.<Owner>builder()
                .data(List.of(OwnerEntity.builder().id(1L).email("teste@email.com").build()))
                .isLast(false)
                .cursor("1")
                .pageSize(10)
                .build();
        when(persistencePort.scroll("teste", 10, "5")).thenReturn(expected);

        ScrollPage<Owner> actual = useCase.scroll("teste", 10, "5");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_no_matching_when_scroll_result_is_empty() {
        ScrollPage<Owner> empty = ScrollPage.<Owner>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();
        when(persistencePort.scroll("teste", 10, "5")).thenReturn(empty);

        assertThatThrownBy(() -> useCase.scroll("teste", 10, "5"))
                .isInstanceOf(NoMatchingOwnersException.class)
                .hasMessage("No matching owners for email: teste");
    }

    @Test
    void should_return_owner_when_found_by_id() {
        OwnerEntity owner = OwnerEntity.builder().id(1L).email("teste@email.com").build();
        when(persistencePort.findById(1L)).thenReturn(Optional.of(owner));

        Owner found = useCase.getById(1L);

        assertThat(found).isSameAs(owner);
    }

    @Test
    void should_throw_when_owner_not_found_by_id() {
        when(persistencePort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getById(99L))
                .isInstanceOf(OwnerNotFoundException.class)
                .hasMessage("Owner not found for id: 99");
    }

    @Test
    void should_throw_when_creating_duplicated_document() {
        OwnerEntity owner = OwnerEntity.builder().email("teste@email.com").document("31058167049").documentType(DocumentType.CPF).build();
        when(persistencePort.findByDocument("31058167049")).thenReturn(Optional.of(OwnerEntity.builder().document("31058167049").build()));

        assertThatThrownBy(() -> useCase.create(owner))
                .isInstanceOf(DuplicatedOwnerException.class)
                .hasMessage("Owner already exists for document: 31058167049");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_create_owner() {
        OwnerEntity owner = OwnerEntity.builder().email("teste@email.com").document("31058167049").documentType(DocumentType.CPF).build();
        when(persistencePort.findByDocument("31058167049")).thenReturn(null);

        when(persistencePort.findByDocument("31058167049"))
                .thenReturn(Optional.empty());

        when(persistencePort.save(owner))
                .thenReturn(owner);

        Owner result = useCase.create(owner);

        assertThat(result).isNotNull();

        verify(persistencePort).save(owner);
    }


    @Test
    void should_update_owner() {
        OwnerEntity existing = OwnerEntity.builder().id(10L).email("teste@email.com")
        .document("31058167049").documentType(DocumentType.CPF).build();
        OwnerEntity incoming = OwnerEntity.builder().id(10L).email("teste@email.com")
        .document("31058167049").documentType(DocumentType.CPF).build();

        when(persistencePort.findByDocument("31058167049")).thenReturn(Optional.of(existing));
        when(persistencePort.findById(10L)).thenReturn(Optional.of(existing));
        when(persistencePort.save(any(Owner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Owner updated = useCase.update(10L, incoming);

        assertThat(updated).isSameAs(incoming);
        assertThat(incoming.getId()).isEqualTo(10L);
    }

    @Test
    void should_throw_when_updating_non_existing_owner() {
        OwnerEntity incoming = OwnerEntity.builder().email("teste@email.com").document("31058167049").documentType(DocumentType.CPF).build();
        when(persistencePort.findById(101L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.update(101L, incoming))
                .isInstanceOf(OwnerNotFoundException.class)
                .hasMessage("Owner not found for id: 101");
    }

    @Test
    void should_throw_when_updating_owner_with_invalid_document() {
        OwnerEntity incoming = OwnerEntity.builder().email("teste@email.com").document("1234").documentType(DocumentType.CPF).build();

        assertThatThrownBy(() -> useCase.update(123L, incoming))
                .isInstanceOf(InvalidDocumentException.class)
                .hasMessage("Invalid document: 1234");
    }

    @Test
    void should_throw_when_updating_owner_with_invalid_email() {
        OwnerEntity incoming = OwnerEntity.builder().email("emailErrado").document("31058167049").documentType(DocumentType.CPF).build();

        assertThatThrownBy(() -> useCase.update(123L, incoming))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessage("Invalid email: emailErrado");
    }

    @Test
    void should_throw_when_updating_owner_with_document_of_another_owner() {
        OwnerEntity incoming = OwnerEntity.builder()
                .email("teste@email.com")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .build();

        OwnerEntity existingOwner = OwnerEntity.builder()
                .id(200L)
                .email("outro@email.com")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .build();

        when(persistencePort.findByDocument("31058167049"))
                .thenReturn(Optional.of(existingOwner));

        assertThatThrownBy(() -> useCase.update(101L, incoming))
                .isInstanceOf(DuplicatedOwnerException.class)
                .hasMessage("Owner already exists for document: 31058167049");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_delete_existing_owner() {
        when(persistencePort.existsById(20L)).thenReturn(true);

        useCase.delete(20L);

        verify(persistencePort).deleteById(20L);
    }

    @Test
    void should_throw_when_deleting_non_existing_owner() {
        when(persistencePort.existsById(22L)).thenReturn(false);

        assertThatThrownBy(() -> useCase.delete(22L))
                .isInstanceOf(OwnerNotFoundException.class)
                .hasMessage("Owner not found for id: 22");

        verify(persistencePort, never()).deleteById(22L);
    }
}
