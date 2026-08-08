package br.com.fiap.postech.adapter.output.catalogservice.persistence.entity;

import br.com.fiap.postech.domain.catalogservices.model.CatalogServices;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "catalog_services")
public class CatalogServicesEntity implements CatalogServices {
    @Builder.Default
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catalog_service_id")
    private Long id = 0L;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @OneToMany(mappedBy = "catalogServices", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NeededSupplyEntity> supplies = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
