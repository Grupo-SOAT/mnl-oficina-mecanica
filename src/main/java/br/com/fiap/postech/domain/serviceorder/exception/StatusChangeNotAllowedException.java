package br.com.fiap.postech.domain.serviceorder.exception;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

public class StatusChangeNotAllowedException extends RuntimeException {

    private final ServiceOrderStatus sourceStatus;
    private final ServiceOrderStatus targetStatus;

    public StatusChangeNotAllowedException(ServiceOrderStatus sourceStatus, ServiceOrderStatus targetStatus) {
        super("Status change not allowed: " + sourceStatus + " -> " + targetStatus);
        this.sourceStatus = sourceStatus;
        this.targetStatus = targetStatus;
    }

    public ServiceOrderStatus getSourceStatus() {
        return sourceStatus;
    }

    public ServiceOrderStatus getTargetStatus() {
        return targetStatus;
    }
}
