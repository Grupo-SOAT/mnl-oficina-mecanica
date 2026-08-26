package br.com.fiap.postech.adapter.output.serviceorder.persistence.entity;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "service_orders")
public class ServiceOrderEntity implements ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_order_id")
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Transient
    @Builder.Default
    private String statusLabel = null;

    @Column(name = "estimated_amount")
    private BigDecimal estimatedAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "partially_rejected_at")
    private LocalDateTime partiallyRejectedAt;

    @Override
    public LocalDateTime getLastStatusChangedAt() {
        final var allStatusChanges = new ArrayList<LocalDateTime>();
        allStatusChanges.add(getCreatedAt());
        allStatusChanges.add(getInspectedAt());
        allStatusChanges.add(getApprovedAt());
        allStatusChanges.add(getCancelledAt());
        allStatusChanges.add(getStartedAt());
        allStatusChanges.add(getCompletedAt());
        allStatusChanges.add(getRejectedAt());
        allStatusChanges.add(getDeliveredAt());
        allStatusChanges.add(getPartiallyRejectedAt());

        return allStatusChanges.stream()
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
