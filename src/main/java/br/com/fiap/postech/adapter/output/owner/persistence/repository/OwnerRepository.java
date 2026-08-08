package br.com.fiap.postech.adapter.output.owner.persistence.repository;

import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<OwnerEntity, Long>, JpaSpecificationExecutor<OwnerEntity> {
    Optional<OwnerEntity> findByDocument(String document);
}
