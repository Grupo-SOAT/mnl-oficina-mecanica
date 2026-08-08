package br.com.fiap.postech.adapter.output.service.persistence.entity;

import br.com.fiap.postech.domain.service.model.NeededSupply;
import br.com.fiap.postech.domain.service.model.Service;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "services")
public class ServiceEntity implements Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long id;

    @Column(name = "service_order_id", nullable = false)
    private Long serviceOrderId;

    @Column(name = "catalog_service_id", nullable = false)
    private Long catalogServiceId;

    @Column(nullable = false)
    private BigDecimal price;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "service_needed_supplies", joinColumns = @JoinColumn(name = "service_id"))
    private List<NeededSupplyEntity> neededSupplyEntities = new ArrayList<>();

    @Column(nullable = false)
    private String status;

    @Transient
    @Builder.Default
    private String statusLabel = null;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Override
    public List<NeededSupply> getNeededSupplies() {
        return neededSupplyEntities.stream()
                .map(n -> NeededSupply.builder()
                        .idSupply(n.getIdSupply())
                        .note(n.getNote())
                        .quantity(n.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void setNeededSupplies(List<NeededSupply> supplies) {
        this.neededSupplyEntities = supplies == null
                ? new ArrayList<>()
                : supplies.stream()
                .map(n -> NeededSupplyEntity.builder()
                        .idSupply(n.getIdSupply())
                        .note(n.getNote())
                        .quantity(n.getQuantity())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
