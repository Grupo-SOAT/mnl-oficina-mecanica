package br.com.fiap.postech.domain.reporting.model;

import lombok.Builder;

@Builder
public record ServiceCalculatedAverageTime(
        Long id,
        String name,
        Long totalCreated,
        Long totalCompleted,
        Double averageTimeBetweenCreateAndComplete,
        Double averageTimeBetweenStartAndComplete,
        Double averageTimeBetweenApproveAndComplete,
        Double averageTimeAwaitingBudgetApproval
) {
}

