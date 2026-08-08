package br.com.fiap.postech.domain.service.exception;

import br.com.fiap.postech.domain.service.exception.reason.ServiceExceptionReason;

public class NegativeSupplyQuantityException extends RuntimeException {
    public final ServiceExceptionReason reason = ServiceExceptionReason.NEGATIVE_SUPPLY_QUANTITY;

    public NegativeSupplyQuantityException(Long supplyId) {
        super("Cannot decrement supply quantity below zero for supply id: " + supplyId);
    }
}
