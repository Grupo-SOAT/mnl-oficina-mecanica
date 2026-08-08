package br.com.fiap.postech.adapter.output.owner.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import br.com.fiap.postech.adapter.output.owner.persistence.repository.OwnerRepository;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.Scroller;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OwnerPersistenceAdapter implements OwnerPersistencePort{
    private final OwnerRepository repository;

    @Override
    public ScrollPage<Owner> scroll(String document, Integer pageSize, String cursor) {
        return Scroller.scroll(
                cursor,
                pageSize,
                (parsedCursor, pageable) -> repository.findAll(buildSpecification(document, parsedCursor), pageable)
                        .getContent()
                        .stream()
                        .map(item -> (Owner) item)
                        .toList()
        );
    }

    private Specification<OwnerEntity> buildSpecification(String document, Long cursor) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            query.orderBy(criteriaBuilder.asc(root.get("id")));

            final var predicates = new ArrayList<Predicate>();

            if (document != null && !document.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                root.get("document"),
                                "%" + document + "%"
                        )
                );
            }

            if (cursor > 0) {
                predicates.add(criteriaBuilder.greaterThan(root.get("id"), cursor));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Optional<Owner> findById(Long id) {
        return repository.findById(id).map(item -> (Owner) item);
    }

    @Override
    public Optional<Owner> findByDocument(String document) {
        return repository.findByDocument(document).map(item -> (Owner) item);
    }

    @Override
    public Owner save(Owner owner) {
        OwnerEntity entity;
        if (owner instanceof OwnerEntity ownerEntity) {
            entity = ownerEntity;
        } else {
            entity = new OwnerEntity();
            entity.setId(owner.getId());
            entity.setName(owner.getName());
            entity.setDocument(owner.getDocument());
            entity.setDocumentType(owner.getDocumentType());
            entity.setPhone(owner.getPhone());
            entity.setEmail(owner.getEmail());
            entity.setCreatedAt(owner.getCreatedAt());
        }

        return repository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
