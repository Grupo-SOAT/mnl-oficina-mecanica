package br.com.fiap.postech.domain.serviceorder.model;

import java.util.List;
import java.util.Set;

/**
 * Domain enum for ServiceOrder status.
 * Mirrors OpenAPI contract but without external dependency.
 */
public enum ServiceOrderStatus {
    PENDING,
    IN_INSPECTION,
    AWAITING_APPROVAL,
    PARTIALLY_REJECTED,
    CANCELLED,
    APPROVED,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED;

    /**
     * Status excluded from the default service order listing (no status filter).
     */
    public static final Set<ServiceOrderStatus> EXCLUDED_FROM_DEFAULT_LISTING = Set.of(COMPLETED, DELIVERED);

    /**
     * Priority order used to sort the default service order listing: IN_PROGRESS, AWAITING_APPROVAL and
     * IN_INSPECTION come first as they require active attention, followed by PENDING. The remaining
     * non-excluded statuses keep their relative declaration order.
     */
    private static final List<ServiceOrderStatus> DEFAULT_LISTING_PRIORITY_ORDER = List.of(
            IN_PROGRESS,
            AWAITING_APPROVAL,
            IN_INSPECTION,
            PENDING,
            PARTIALLY_REJECTED,
            CANCELLED,
            APPROVED
    );

    public static List<ServiceOrderStatus> defaultListingPriorityOrder() {
        return DEFAULT_LISTING_PRIORITY_ORDER;
    }
}
