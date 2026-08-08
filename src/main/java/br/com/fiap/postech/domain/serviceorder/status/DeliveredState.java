package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;

/**
 * Terminal state: DELIVERED. No transitions allowed.
 */
public class DeliveredState extends ServiceOrderState {

    public DeliveredState() {
        super(ServiceOrderStatus.DELIVERED);
    }
}
