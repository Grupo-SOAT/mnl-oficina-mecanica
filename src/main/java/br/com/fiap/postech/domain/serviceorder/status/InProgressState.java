package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

import java.util.Set;

/**
 * State representing an order in progress.
 * Allowed transitions: COMPLETED, CANCELLED.
 */
public class InProgressState extends ServiceOrderState {

    private static final Set<ServiceOrderStatus> ALLOWED_TRANSITIONS = 
        Set.of(ServiceOrderStatus.COMPLETED, ServiceOrderStatus.CANCELLED);

    public InProgressState() {
        super(ServiceOrderStatus.IN_PROGRESS);
    }

    @Override
    public void transitionTo(ServiceOrderStatus targetStatus) {
        if (ALLOWED_TRANSITIONS.contains(targetStatus)) {
            return;
        }
        super.transitionTo(targetStatus);
    }
}
