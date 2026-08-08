package br.com.fiap.postech.adapter.output.catalogservice.persistence.entity;

import br.com.fiap.postech.adapter.output.supply.persistence.entity.SupplyEntity;
import br.com.fiap.postech.domain.catalogservices.model.NeededSupply;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "service_supplies")
public class NeededSupplyEntity implements NeededSupply {
    @Builder.Default
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_supplies_id")
    private Long servicesSuppliesId = 0L;

    @ManyToOne
    @JoinColumn(name = "catalog_service_id")
    private CatalogServicesEntity catalogServices;

    @ManyToOne
    @JoinColumn(name = "supply_id", nullable = false)
    private SupplyEntity supply;

    @Column(nullable = false)
    private Integer supplyAmount;

}
