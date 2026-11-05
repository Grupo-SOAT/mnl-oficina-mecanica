package br.com.fiap.postech.adapter.output.owner.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import br.com.fiap.postech.adapter.input.api.model.DocumentType;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.owner.persistence.repository.OwnerRepository;
import br.com.fiap.postech.adapter.output.persistence.SpecificationProbe;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.model.Owner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OwnerPersistenceAdapterTest {
    @Mock
    private OwnerRepository repository;

    @InjectMocks
    private OwnerPersistenceAdapter adapter;
    
    @Test
    void should_scroll_without_email_filter_using_find_all_after_cursor() {
        OwnerEntity first = OwnerEntity.builder().id(11L).email("A@email.com").build();
        OwnerEntity second = OwnerEntity.builder().id(12L).email("B@email.com").build();
        OwnerEntity third = OwnerEntity.builder().id(13L).email("C@email.com").build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(first, second, third)));

        ScrollPage<Owner> page = adapter.scroll(null, 2, "10");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));

        assertThat(page.data()).hasSize(2);
        assertThat(page.isLast()).isFalse();
    }

    @Test
    void should_scroll_with_document_filter_using_find_by_document_after_cursor() {
        OwnerEntity only = OwnerEntity.builder().id(1L).document("84779441056").build();
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(only)));

        ScrollPage<Owner> page = adapter.scroll("84779441056", 2, "invalid-cursor");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));

        assertThat(page.data()).hasSize(1);
        assertThat(page.isLast()).isTrue();
        assertThat(page.cursor()).isNull();
    }

    @Test
    void should_map_find_by_id() {
        OwnerEntity entity = OwnerEntity.builder().id(5L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(entity));

        Optional<Owner> found = adapter.findById(5L);

        assertThat(found).containsSame(entity);
    }

    @Test
    void should_map_find_by_document() {
        OwnerEntity entity = OwnerEntity.builder().id(8L).document("31058167049").build();
        when(repository.findByDocument("31058167049")).thenReturn(Optional.of(entity));

        Optional<Owner> found = adapter.findByDocument("31058167049");

        assertThat(found).containsSame(entity);
    }

    @Test
    void should_save_when_owner_already_entity() {
        OwnerEntity entity = OwnerEntity.builder().id(9L).build();
        when(repository.save(entity)).thenReturn(entity);

        Owner saved = adapter.save(entity);

        assertThat(saved).isSameAs(entity);
    }

    @Test
    void should_save_when_owner_is_not_entity() {
        Owner owner = new TestOwner(40L, "Nome", "31058167049", DocumentType.CPF, "12314153", "teste40@email.com");
        owner.setCreatedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));

        when(repository.save(any(OwnerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Owner saved = adapter.save(owner);

        OwnerEntity expected = OwnerEntity.builder()
                .id(40L)
                .name("Nome")
                .document("31058167049")
                .documentType(DocumentType.CPF)
                .phone("12314153")
                .email("teste40@email.com")
                .createdAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
        assertThat(saved).usingRecursiveComparison().isEqualTo(expected);
    }

    @Test
    void should_delegate_delete_by_id() {
        adapter.deleteById(77L);
        verify(repository).deleteById(77L);
    }

    @Test
    void should_delegate_exists_by_id() {
        when(repository.existsById(55L)).thenReturn(true);

        boolean exists = adapter.existsById(55L);

        assertThat(exists).isTrue();
    }

    @Test
    void should_report_absence_when_owner_does_not_exist() {
        when(repository.existsById(55L)).thenReturn(false);

        assertThat(adapter.existsById(55L)).isFalse();
    }

    @Test
    void should_build_empty_specification_when_document_is_blank_and_cursor_is_zero() {
        SpecificationProbe probe = evaluateScrollSpecification("   ", null);

        assertThat(probe.andPredicates()).isEmpty();
        probe.verifyOrderWasApplied();
    }

    @Test
    void should_build_document_predicate_when_document_is_informed() {
        SpecificationProbe probe = evaluateScrollSpecification("8477", null);

        assertThat(probe.andPredicates()).hasSize(1);
        verify(probe.builder()).like(any(), eq("%8477%"));
    }

    @Test
    void should_build_cursor_predicate_when_cursor_is_positive() {
        SpecificationProbe probe = evaluateScrollSpecification(null, "10");

        assertThat(probe.andPredicates()).hasSize(1);
        verify(probe.builder()).greaterThan(any(), eq(10L));
    }

    @Test
    void should_build_document_and_cursor_predicates_together() {
        SpecificationProbe probe = evaluateScrollSpecification("8477", "10");

        assertThat(probe.andPredicates()).hasSize(2);
    }

    @Test
    void should_preserve_active_flag_when_saving_existing_owner() {
        OwnerEntity current = OwnerEntity.builder().id(40L).active(false).build();
        when(repository.findById(40L)).thenReturn(Optional.of(current));
        when(repository.save(any(OwnerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OwnerEntity toSave = OwnerEntity.builder().id(40L).name("Nome").build();
        Owner saved = adapter.save(toSave);

        assertThat(saved).usingRecursiveComparison().isEqualTo(OwnerEntity.builder().id(40L).name("Nome").active(false).build());
    }

    @Test
    void should_not_lookup_existing_owner_when_id_is_not_positive() {
        when(repository.save(any(OwnerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(OwnerEntity.builder().id(0L).name("Novo").build());

        verify(repository, never()).findById(anyLong());
    }

    private SpecificationProbe evaluateScrollSpecification(String document, String cursor) {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        adapter.scroll(document, 10, cursor);

        ArgumentCaptor<Specification<OwnerEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), any(Pageable.class));
        return SpecificationProbe.evaluate(captor.getValue());
    }

    private static final class TestOwner implements Owner {

        private Long id;
        private String name;
        private String document;
        private DocumentType documentType;
        private String phone;
        private String email;
        private java.time.LocalDateTime createdAt;

        private TestOwner(Long id,
                        String name,
                        String document,
                        DocumentType documentType,
                        String phone,
                        String email) {
            this.id = id;
            this.name = name;
            this.document = document;
            this.documentType = documentType;
            this.phone = phone;
            this.email = email;
        }

        @Override
        public Long getId() {
            return id;
        }

        @Override
        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String getDocument() {
            return document;
        }

        @Override
        public void setDocument(String document) {
            this.document = document;
        }

        @Override
        public DocumentType getDocumentType() {
            return documentType;
        }

        @Override
        public void setDocumentType(DocumentType documentType) {
            this.documentType = documentType;
        }

        @Override
        public String getPhone() {
            return phone;
        }

        @Override
        public void setPhone(String phone) {
            this.phone = phone;
        }

        @Override
        public String getEmail() {
            return email;
        }

        @Override
        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public java.time.LocalDateTime getCreatedAt() {
            return createdAt;
        }

        @Override
        public void setCreatedAt(java.time.LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

}
