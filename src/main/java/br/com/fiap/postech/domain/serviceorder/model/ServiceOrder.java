package br.com.fiap.postech.domain.serviceorder.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ServiceOrder {
    Long getId();
    void setId(Long id);

    Long getClientId();
    void setClientId(Long clientId);

    Long getVehicleId();
    void setVehicleId(Long vehicleId);

    String getDescription();
    void setDescription(String description);

    String getStatus();
    void setStatus(String status);

    String getStatusLabel();
    void setStatusLabel(String statusLabel);

    BigDecimal getEstimatedAmount();
    void setEstimatedAmount(BigDecimal estimatedAmount);

    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);

    LocalDateTime getInspectedAt();
    void setInspectedAt(LocalDateTime inspectedAt);

    LocalDateTime getApprovedAt();
    void setApprovedAt(LocalDateTime approvedAt);

    LocalDateTime getRejectedAt();
    void setRejectedAt(LocalDateTime rejectedAt);

    LocalDateTime getCancelledAt();
    void setCancelledAt(LocalDateTime cancelledAt);

    LocalDateTime getStartedAt();
    void setStartedAt(LocalDateTime startedAt);

    LocalDateTime getCompletedAt();
    void setCompletedAt(LocalDateTime completedAt);

    LocalDateTime getDeliveredAt();
    void setDeliveredAt(LocalDateTime deliveredAt);

    LocalDateTime getPartiallyRejectedAt();
    void setPartiallyRejectedAt(LocalDateTime partiallyRejectedAt);
}
