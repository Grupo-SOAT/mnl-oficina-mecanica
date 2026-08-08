package br.com.fiap.postech.adapter.output.service.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class NeededSupplyEntity {
    private Long idSupply;
    private String note;
    private Integer quantity;
}
