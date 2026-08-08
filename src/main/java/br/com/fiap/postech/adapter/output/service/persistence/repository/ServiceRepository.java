package br.com.fiap.postech.adapter.output.service.persistence.repository;

import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Long>, JpaSpecificationExecutor<ServiceEntity> {

    Optional<ServiceEntity> findByIdAndServiceOrderId(Long id, Long serviceOrderId);

    List<ServiceEntity> findByCatalogServiceId(Long catalogServiceId);

    @Query("SELECT s FROM ServiceEntity s WHERE s.serviceOrderId = :serviceOrderId")
    List<ServiceEntity> findAllByServiceOrderId(@Param("serviceOrderId") Long serviceOrderId);
}
