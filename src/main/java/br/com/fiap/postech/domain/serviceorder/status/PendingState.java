package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

import java.util.List;
import java.util.Set;

/**
 * Initial state: PENDING.
 * From here, the only allowed transitions are PENDING -> IN_INSPECTION and PENDING -> CANCELLED.
 */
public class PendingState extends ServiceOrderState {

    private static final Set<ServiceOrderStatus> ALLOWED_TRANSITIONS = 
        Set.of(ServiceOrderStatus.IN_INSPECTION, ServiceOrderStatus.CANCELLED);

    public PendingState() {
        super(ServiceOrderStatus.PENDING);
    }

    @Override
    public void transitionTo(ServiceOrderStatus targetStatus) {
        if (ALLOWED_TRANSITIONS.contains(targetStatus)) {
            return;
        }
        super.transitionTo(targetStatus);
    }
}
