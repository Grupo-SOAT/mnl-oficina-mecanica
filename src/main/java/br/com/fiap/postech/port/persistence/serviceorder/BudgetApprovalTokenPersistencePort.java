package br.com.fiap.postech.port.persistence.serviceorder;

import br.com.fiap.postech.domain.serviceorder.model.BudgetApprovalToken;

import java.util.Optional;

public interface BudgetApprovalTokenPersistencePort {

    BudgetApprovalToken create(BudgetApprovalToken token);

    Optional<BudgetApprovalToken> findByServiceOrderId(Long serviceOrderId);

    void markUsed(Long serviceOrderId);
}
