package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

/**
 * Placeholder state for PARTIALLY_REJECTED.
 * In V2, attempting to transition into this status should return 501 at use case/controller level.
 */
public class PartiallyRejectedState extends ServiceOrderState {

    public PartiallyRejectedState() {
        super(ServiceOrderStatus.PARTIALLY_REJECTED);
    }
}
