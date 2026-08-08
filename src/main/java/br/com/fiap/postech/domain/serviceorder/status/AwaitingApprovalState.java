package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

import java.util.Set;

/**
 * State representing an order waiting for client approval.
 * Allowed transitions: APPROVED, CANCELLED, PARTIALLY_REJECTED.
 */
public class AwaitingApprovalState extends ServiceOrderState {

    private static final Set<ServiceOrderStatus> ALLOWED_TRANSITIONS = 
        Set.of(ServiceOrderStatus.APPROVED, ServiceOrderStatus.CANCELLED, 
               ServiceOrderStatus.PARTIALLY_REJECTED);

    public AwaitingApprovalState() {
        super(ServiceOrderStatus.AWAITING_APPROVAL);
    }

    @Override
    public void transitionTo(ServiceOrderStatus targetStatus) {
        if (ALLOWED_TRANSITIONS.contains(targetStatus)) {
            return;
        }
        super.transitionTo(targetStatus);
    }
}
