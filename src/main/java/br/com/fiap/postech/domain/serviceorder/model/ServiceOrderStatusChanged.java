package br.com.fiap.postech.domain.serviceorder.model;

import java.time.LocalDateTime;

public record ServiceOrderStatusChanged(
        Long id,
        ServiceOrderStatus from,
        ServiceOrderStatus to,
        LocalDateTime lastStatusChangedAt,
        LocalDateTime currentStatusChangedAt
) {
}
