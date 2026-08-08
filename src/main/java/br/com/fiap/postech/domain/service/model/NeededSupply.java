package br.com.fiap.postech.domain.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NeededSupply {
    private Long idSupply;
    private String note;
    private Integer quantity;
}
