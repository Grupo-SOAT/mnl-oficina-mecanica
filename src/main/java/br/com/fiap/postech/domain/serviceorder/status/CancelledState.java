package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

import java.util.Set;

/**
 * State representing a cancelled order.
 * Allowed transitions: DELIVERED.
 */
public class CancelledState extends ServiceOrderState {

    private static final Set<ServiceOrderStatus> ALLOWED_TRANSITIONS = 
        Set.of(ServiceOrderStatus.DELIVERED);

    public CancelledState() {
        super(ServiceOrderStatus.CANCELLED);
    }

    @Override
    public void transitionTo(ServiceOrderStatus targetStatus) {
        if (ALLOWED_TRANSITIONS.contains(targetStatus)) {
            return;
        }
        super.transitionTo(targetStatus);
    }
}
