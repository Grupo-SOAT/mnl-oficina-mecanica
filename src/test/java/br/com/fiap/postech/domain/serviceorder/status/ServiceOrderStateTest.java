package br.com.fiap.postech.domain.serviceorder.status;

import br.com.fiap.postech.domain.serviceorder.exception.StatusChangeNotAllowedException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderStateTest {

    @Test
    void should_allow_pending_to_in_inspection() {
        var state = new PendingState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.IN_INSPECTION));
    }

    @Test
    void should_allow_pending_to_cancelled() {
        var state = new PendingState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.CANCELLED));
    }

    @Test
    void should_reject_pending_to_approved() {
        var state = new PendingState();

        assertThatThrownBy(() -> state.transitionTo(ServiceOrderStatus.APPROVED))
                .isInstanceOf(StatusChangeNotAllowedException.class)
                .hasMessage("Status change not allowed: PENDING -> APPROVED");
    }

    @Test
    void should_allow_in_inspection_to_awaiting_approval() {
        var state = new InInspectionState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.AWAITING_APPROVAL));
    }

    @Test
    void should_allow_in_inspection_to_cancelled() {
        var state = new InInspectionState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.CANCELLED));
    }

    @Test
    void should_allow_awaiting_approval_to_approved() {
        var state = new AwaitingApprovalState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.APPROVED));
    }

    @Test
    void should_allow_awaiting_approval_to_cancelled() {
        var state = new AwaitingApprovalState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.CANCELLED));
    }

    @Test
    void should_allow_awaiting_approval_to_partially_rejected_in_state_machine() {
        var state = new AwaitingApprovalState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.PARTIALLY_REJECTED));
    }

    @Test
    void should_allow_approved_to_in_progress() {
        var state = new ApprovedState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.IN_PROGRESS));
    }

    @Test
    void should_allow_approved_to_cancelled() {
        var state = new ApprovedState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.CANCELLED));
    }

    @Test
    void should_allow_in_progress_to_completed() {
        var state = new InProgressState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.COMPLETED));
    }

    @Test
    void should_allow_in_progress_to_cancelled() {
        var state = new InProgressState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.CANCELLED));
    }

    @Test
    void should_allow_completed_to_delivered() {
        var state = new CompletedState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.DELIVERED));
    }

    @Test
    void should_allow_cancelled_to_delivered() {
        var state = new CancelledState();

        assertThatNoException().isThrownBy(() -> state.transitionTo(ServiceOrderStatus.DELIVERED));
    }

    @Test
    void should_reject_delivered_to_any_other_status() {
        var state = new DeliveredState();

        assertThatThrownBy(() -> state.transitionTo(ServiceOrderStatus.PENDING))
                .isInstanceOf(StatusChangeNotAllowedException.class)
                .hasMessage("Status change not allowed: DELIVERED -> PENDING");
    }

    @Test
    void should_return_state_factory_for_each_status() {
        assertThat(ServiceOrderState.of(ServiceOrderStatus.PENDING)).isInstanceOf(PendingState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.IN_INSPECTION)).isInstanceOf(InInspectionState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.AWAITING_APPROVAL)).isInstanceOf(AwaitingApprovalState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.APPROVED)).isInstanceOf(ApprovedState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.IN_PROGRESS)).isInstanceOf(InProgressState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.COMPLETED)).isInstanceOf(CompletedState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.CANCELLED)).isInstanceOf(CancelledState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.DELIVERED)).isInstanceOf(DeliveredState.class);
        assertThat(ServiceOrderState.of(ServiceOrderStatus.PARTIALLY_REJECTED)).isInstanceOf(PartiallyRejectedState.class);
    }
}
