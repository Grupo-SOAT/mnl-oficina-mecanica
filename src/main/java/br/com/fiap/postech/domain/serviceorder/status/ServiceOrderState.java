package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.exception.StatusChangeNotAllowedException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

/**
 * Abstract base class for ServiceOrder states.
 * Default behavior: any transition throws StatusChangeNotAllowedException.
 * Subclasses override transitionTo() to define allowed transitions.
 */
public abstract class ServiceOrderState {

    private final ServiceOrderStatus status;

    protected ServiceOrderState(ServiceOrderStatus status) {
        this.status = status;
    }

    public final ServiceOrderStatus getStatus() {
        return status;
    }

    /**
     * Validates a status transition.
     * @param targetStatus the target status to transition to
     * @throws StatusChangeNotAllowedException if transition is not allowed
     */
    public void transitionTo(ServiceOrderStatus targetStatus) {
        throw new StatusChangeNotAllowedException(this.status, targetStatus);
    }

    /**
     * Factory method to get the appropriate state instance for a given status.
     */
    public static ServiceOrderState of(ServiceOrderStatus status) {
        return switch (status) {
            case PENDING -> new PendingState();
            case IN_INSPECTION -> new InInspectionState();
            case AWAITING_APPROVAL -> new AwaitingApprovalState();
            case APPROVED -> new ApprovedState();
            case IN_PROGRESS -> new InProgressState();
            case COMPLETED -> new CompletedState();
            case CANCELLED -> new CancelledState();
            case DELIVERED -> new DeliveredState();
            case PARTIALLY_REJECTED -> new PartiallyRejectedState();
        };
    }
}
