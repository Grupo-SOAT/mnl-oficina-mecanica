package br.com.fiap.postech.adapter.output.catalogservice.persistence.repository;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.entity.CatalogServicesEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CatalogServicesRepository extends JpaRepository<CatalogServicesEntity, Long>, JpaSpecificationExecutor<CatalogServicesEntity> {
    @EntityGraph(attributePaths = "supplies")
    Optional<CatalogServicesEntity> findWithSuppliesById(Long id);

    @EntityGraph(attributePaths = "supplies")
    Optional<CatalogServicesEntity> findByName(String name);
}
