package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

import java.util.Set;

/**
 * State representing an order under inspection.
 * Allowed transitions: AWAITING_APPROVAL, CANCELLED.
 */
public class InInspectionState extends ServiceOrderState {

    private static final Set<ServiceOrderStatus> ALLOWED_TRANSITIONS = 
        Set.of(ServiceOrderStatus.AWAITING_APPROVAL, ServiceOrderStatus.CANCELLED);

    public InInspectionState() {
        super(ServiceOrderStatus.IN_INSPECTION);
    }

    @Override
    public void transitionTo(ServiceOrderStatus targetStatus) {
        if (ALLOWED_TRANSITIONS.contains(targetStatus)) {
            return;
        }
        super.transitionTo(targetStatus);
    }
}
