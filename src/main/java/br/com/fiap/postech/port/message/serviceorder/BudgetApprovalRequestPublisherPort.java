package br.com.fiap.postech.port.message.serviceorder;

public interface BudgetApprovalRequestPublisherPort {

    void publish(Long serviceOrderId, String token);
}
