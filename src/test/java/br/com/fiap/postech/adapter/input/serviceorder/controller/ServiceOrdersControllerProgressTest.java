package br.com.fiap.postech.adapter.input.serviceorder.controller;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.input.api.model.BudgetDecisionRequest;
import br.com.fiap.postech.adapter.input.api.model.ServiceOrderAction;
import br.com.fiap.postech.adapter.input.api.model.ServiceOrderActionRequest;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.serviceorder.exception.PartialBudgetRejectionNotImplementedException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.serviceorder.exception.StatusChangeNotAllowedException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.domain.serviceorder.usecase.ChangeServiceOrderStatusUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrdersControllerProgressTest {

    @Mock
    private ChangeServiceOrderStatusUseCase changeStatusUseCase;

    @InjectMocks
    private ServiceOrdersController controller;

    @Test
    void should_call_change_status_use_case_for_register_progress() {
        var request = new ServiceOrderActionRequest();
        request.setAction(ServiceOrderAction.START_INSPECTION);
        var updated = ServiceOrderEntity.builder()
                .id(1L)
                .status("IN_INSPECTION")
                .build();
        when(changeStatusUseCase.registerProgress(1L, ServiceOrderAction.START_INSPECTION, null))
                .thenReturn(updated);

        var response = controller.registerProgress(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void should_call_change_status_use_case_for_register_client_decision() {
        var request = new BudgetDecisionRequest();
        request.setDecision(BudgetDecision.APPROVE);
        var updated = ServiceOrderEntity.builder()
                .id(1L)
                .status("APPROVED")
                .build();
        when(changeStatusUseCase.registerClientDecision(1L, BudgetDecision.APPROVE))
                .thenReturn(updated);

        var response = controller.registerClientDecision(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void should_handle_status_change_not_allowed_exception() {
        var exception = new StatusChangeNotAllowedException(
                ServiceOrderStatus.PENDING,
                ServiceOrderStatus.DELIVERED
        );
        var response = controller.handleStatusChangeNotAllowed(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(409);
        assertThat(response.getBody().getReason()).isEqualTo("STATUS_CHANGE_NOT_ALLOWED");
    }

    @Test
    void should_handle_partial_budget_rejection_not_implemented_exception() {
        var exception = new PartialBudgetRejectionNotImplementedException();
        var response = controller.handlePartialBudgetRejectionNotImplemented(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(501);
        assertThat(response.getBody().getReason()).isEqualTo("PARTIAL_BUDGET_REJECTION_NOT_IMPLEMENTED");
    }
}
