package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

import java.util.Set;

/**
 * State representing an approved order.
 * Allowed transitions: IN_PROGRESS, CANCELLED.
 */
public class ApprovedState extends ServiceOrderState {

    private static final Set<ServiceOrderStatus> ALLOWED_TRANSITIONS = 
        Set.of(ServiceOrderStatus.IN_PROGRESS, ServiceOrderStatus.CANCELLED);

    public ApprovedState() {
        super(ServiceOrderStatus.APPROVED);
    }

    @Override
    public void transitionTo(ServiceOrderStatus targetStatus) {
        if (ALLOWED_TRANSITIONS.contains(targetStatus)) {
            return;
        }
        super.transitionTo(targetStatus);
    }
}
