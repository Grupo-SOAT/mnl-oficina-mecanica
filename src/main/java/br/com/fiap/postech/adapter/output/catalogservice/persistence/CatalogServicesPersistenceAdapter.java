package br.com.fiap.postech.adapter.output.catalogservice.persistence;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.catalogservice.persistence.repository.CatalogServicesRepository;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.Scroller;
import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogServicesPersistenceAdapter implements CatalogServicesPersistencePort {
    private final CatalogServicesRepository repository;

    @Override
    public ScrollPage<CatalogServices> scroll(Long id, String name, Integer pageSize, String cursor) {
        return Scroller.scroll(
                cursor,
                pageSize,
                (parsedCursor, pageable) -> repository.findAll(buildSpecification(id, name, parsedCursor), pageable)
                        .getContent()
                        .stream()
                        .map(item -> (CatalogServices) item)
                        .toList()
        );
    }

    private Specification<CatalogServicesEntity> buildSpecification(Long id, String name, Long cursor) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            query.orderBy(criteriaBuilder.asc(root.get("id")));

            final var predicates = new ArrayList<Predicate>();

            if (id != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), id));
            }

            if (name != null && !name.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%"
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
    public CatalogServices save(CatalogServices catalogServices) {
        CatalogServicesEntity entity;

        if (catalogServices instanceof CatalogServicesEntity catalogServicesEntity) {
            entity = catalogServicesEntity;
        } else {
            entity = new CatalogServicesEntity();
            entity.setId(catalogServices.getId());
            entity.setName(catalogServices.getName().toUpperCase());
            entity.setDescription(catalogServices.getDescription());
            entity.setBasePrice(catalogServices.getBasePrice());
        }
        if (entity.getSupplies() != null) {
            for (NeededSupplyEntity supply : entity.getSupplies()) {
                supply.setCatalogServices(entity);
            }
        }
        return repository.save(entity);
    }

    @Override
    public Optional<CatalogServices> findByName(String name) {
        return repository.findByName(name).map(item -> (CatalogServices) item);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Optional<CatalogServices> findById(Long id) {
        return repository.findWithSuppliesById(id).map(item -> (CatalogServices) item);
    }
}
