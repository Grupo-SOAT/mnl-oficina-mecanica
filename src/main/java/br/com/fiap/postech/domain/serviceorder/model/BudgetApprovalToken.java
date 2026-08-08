package br.com.fiap.postech.domain.serviceorder.model;

import java.time.Instant;

public record BudgetApprovalToken(
        Long id,
        Long serviceOrderId,
        String token,
        Instant expiresAt,
        Instant createdAt,
        Instant usedAt
) {

    public BudgetApprovalToken(Long serviceOrderId, String token, Instant expiresAt) {
        this(null, serviceOrderId, token, expiresAt, Instant.now(), null);
    }
}
