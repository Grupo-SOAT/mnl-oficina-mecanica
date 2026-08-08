package br.com.fiap.postech.domain.supply.exception;

import br.com.fiap.postech.domain.supply.exception.reason.SupplyExceptionReason;

public class DuplicatedSupplyException extends RuntimeException {
    public SupplyExceptionReason reason = SupplyExceptionReason.SUPPLY_CONFLICT_DUPLICATED_SKU;
    public DuplicatedSupplyException(String sku) {
        super("Supply already exists for sku: " + sku);
    }
}
