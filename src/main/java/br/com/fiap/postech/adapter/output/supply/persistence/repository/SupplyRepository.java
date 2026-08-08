package br.com.fiap.postech.adapter.output.supply.persistence.repository;

import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<SupplyEntity, Long>, JpaSpecificationExecutor<SupplyEntity> {
    Optional<SupplyEntity> findBySku(String sku);

    @Query("SELECT s FROM SupplyEntity s WHERE s.id > :cursor ORDER BY s.id ASC")
    List<SupplyEntity> findAllAfterCursor(
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("SELECT s FROM SupplyEntity s WHERE LOWER(s.sku) LIKE LOWER(CONCAT('%', :sku, '%')) AND s.id > :cursor ORDER BY s.id ASC")
    List<SupplyEntity> findBySkuAfterCursor(
            @Param("sku") String sku,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
