package br.com.fiap.postech.domain.service.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface Service {
    Long getId();
    void setId(Long id);

    Long getServiceOrderId();
    void setServiceOrderId(Long serviceOrderId);

    Long getCatalogServiceId();
    void setCatalogServiceId(Long catalogServiceId);

    BigDecimal getPrice();
    void setPrice(BigDecimal price);

    List<NeededSupply> getNeededSupplies();
    void setNeededSupplies(List<NeededSupply> neededSupplies);

    String getStatus();
    void setStatus(String status);

    String getStatusLabel();
    void setStatusLabel(String statusLabel);

    LocalDateTime getCreatedAt();
    void setCreatedAt(LocalDateTime createdAt);

    LocalDateTime getUpdatedAt();
    void setUpdatedAt(LocalDateTime updatedAt);

    LocalDateTime getRejectedAt();
    void setRejectedAt(LocalDateTime rejectedAt);

    LocalDateTime getCancelledAt();
    void setCancelledAt(LocalDateTime cancelledAt);

    LocalDateTime getApprovedAt();
    void setApprovedAt(LocalDateTime approvedAt);

    LocalDateTime getStartedAt();
    void setStartedAt(LocalDateTime startedAt);

    LocalDateTime getCompletedAt();
    void setCompletedAt(LocalDateTime completedAt);
}
