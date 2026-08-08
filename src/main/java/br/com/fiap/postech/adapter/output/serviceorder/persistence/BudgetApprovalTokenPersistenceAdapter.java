package br.com.fiap.postech.adapter.output.serviceorder.persistence;

import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.BudgetApprovalTokenEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.repository.BudgetApprovalTokenRepository;
import br.com.fiap.postech.domain.serviceorder.model.BudgetApprovalToken;
import br.com.fiap.postech.port.persistence.serviceorder.BudgetApprovalTokenPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BudgetApprovalTokenPersistenceAdapter implements BudgetApprovalTokenPersistencePort {

    private final BudgetApprovalTokenRepository repository;

    @Override
    public BudgetApprovalToken create(BudgetApprovalToken domainToken) {
        BudgetApprovalTokenEntity entity = BudgetApprovalTokenEntity.builder()
                .serviceOrderId(domainToken.serviceOrderId())
                .token(domainToken.token())
                .expiresAt(domainToken.expiresAt())
                .createdAt(domainToken.createdAt())
                .build();
        BudgetApprovalTokenEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<BudgetApprovalToken> findByServiceOrderId(Long serviceOrderId) {
        return repository.findByServiceOrderId(serviceOrderId)
                .map(this::toDomain);
    }

    @Override
    public void markUsed(Long serviceOrderId) {
        repository.findByServiceOrderId(serviceOrderId).ifPresent(entity -> {
            entity.setUsedAt(java.time.Instant.now());
            repository.save(entity);
        });
    }

    private BudgetApprovalToken toDomain(BudgetApprovalTokenEntity entity) {
        return new BudgetApprovalToken(
                entity.getId(),
                entity.getServiceOrderId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUsedAt()
        );
    }
}
