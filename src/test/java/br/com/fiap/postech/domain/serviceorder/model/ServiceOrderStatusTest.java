package br.com.fiap.postech.domain.serviceorder.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderStatusTest {

    @Test
    void should_exclude_completed_and_delivered_from_default_listing() {
        assertThat(ServiceOrderStatus.EXCLUDED_FROM_DEFAULT_LISTING)
                .containsExactlyInAnyOrder(ServiceOrderStatus.COMPLETED, ServiceOrderStatus.DELIVERED);
    }

    @Test
    void should_rank_active_statuses_before_remaining_statuses_in_default_listing_priority() {
        assertThat(ServiceOrderStatus.defaultListingPriorityOrder())
                .containsExactly(
                        ServiceOrderStatus.IN_PROGRESS,
                        ServiceOrderStatus.AWAITING_APPROVAL,
                        ServiceOrderStatus.IN_INSPECTION,
                        ServiceOrderStatus.PENDING,
                        ServiceOrderStatus.PARTIALLY_REJECTED,
                        ServiceOrderStatus.CANCELLED,
                        ServiceOrderStatus.APPROVED
                );
    }

    @Test
    void should_not_include_excluded_statuses_in_default_listing_priority_order() {
        assertThat(ServiceOrderStatus.defaultListingPriorityOrder())
                .doesNotContainAnyElementsOf(ServiceOrderStatus.EXCLUDED_FROM_DEFAULT_LISTING);
    }
}
