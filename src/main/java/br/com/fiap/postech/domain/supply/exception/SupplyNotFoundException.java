package br.com.fiap.postech.domain.supply.exception;

import br.com.fiap.postech.domain.supply.exception.reason.SupplyExceptionReason;

public class SupplyNotFoundException extends RuntimeException {
    public SupplyExceptionReason reason = SupplyExceptionReason.SUPPLY_NOT_FOUND;
    public SupplyNotFoundException(Long id) {
        super("Supply not found for id: " + id);
    }
}
