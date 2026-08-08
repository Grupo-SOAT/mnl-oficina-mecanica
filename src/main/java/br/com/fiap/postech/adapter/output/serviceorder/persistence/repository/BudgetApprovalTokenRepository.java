package br.com.fiap.postech.adapter.output.serviceorder.persistence.repository;

import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.BudgetApprovalTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetApprovalTokenRepository extends JpaRepository<BudgetApprovalTokenEntity, Long> {

    Optional<BudgetApprovalTokenEntity> findByServiceOrderId(Long serviceOrderId);
}
