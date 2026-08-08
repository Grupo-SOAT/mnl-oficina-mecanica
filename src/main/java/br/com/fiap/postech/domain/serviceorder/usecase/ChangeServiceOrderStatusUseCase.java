package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.input.api.model.BudgetDecision;
import br.com.fiap.postech.adapter.input.api.model.ServiceOrderAction;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.domain.service.usecase.ChangeServiceStatusUseCase;
import br.com.fiap.postech.domain.serviceorder.exception.PartialBudgetRejectionNotImplementedException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.domain.serviceorder.status.ServiceOrderState;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;

import java.time.LocalDateTime;

public class ChangeServiceOrderStatusUseCase {

    private final ServiceOrderPersistencePort serviceOrderPersistencePort;
    private final ServicePersistencePort servicePersistencePort;
    private final ChangeServiceStatusUseCase changeServiceStatusUseCase;
    private final FinalizeInspectionUseCase finalizeInspectionUseCase;
    private final EstimateServiceOrderAmountUseCase estimateServiceOrderAmountUseCase;
    private final ServiceOrderStatusLabelPort statusLabelPort;

    public ChangeServiceOrderStatusUseCase(
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            ServicePersistencePort servicePersistencePort,
            ChangeServiceStatusUseCase changeServiceStatusUseCase,
            FinalizeInspectionUseCase finalizeInspectionUseCase,
            EstimateServiceOrderAmountUseCase estimateServiceOrderAmountUseCase,
            ServiceOrderStatusLabelPort statusLabelPort
    ) {
        this.serviceOrderPersistencePort = serviceOrderPersistencePort;
        this.servicePersistencePort = servicePersistencePort;
        this.changeServiceStatusUseCase = changeServiceStatusUseCase;
        this.finalizeInspectionUseCase = finalizeInspectionUseCase;
        this.estimateServiceOrderAmountUseCase = estimateServiceOrderAmountUseCase;
        this.statusLabelPort = statusLabelPort;
    }

    public ServiceOrder registerProgress(Long id, ServiceOrderAction action) {
        return registerProgress(id, action, null);
    }

    public ServiceOrder registerProgress(Long id, ServiceOrderAction action, Long relatedServiceId) {
        final var serviceOrder = serviceOrderPersistencePort.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));

        final var currentStatus = ServiceOrderStatus.valueOf(serviceOrder.getStatus());
        final var targetStatus = resolveProgressTarget(action);

        validateTransition(currentStatus, targetStatus);

        // Handle service-related actions
        if (isServiceAction(action) && relatedServiceId != null) {
            handleServiceAction(action, relatedServiceId, serviceOrder);
        } else {
            applyStatusTransition(serviceOrder, targetStatus);
        }

        var saved = serviceOrderPersistencePort.save(serviceOrder);
        saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        afterProgressSave(saved, targetStatus);
        return saved;
    }

    private void afterProgressSave(ServiceOrder serviceOrder, ServiceOrderStatus targetStatus) {
        if (targetStatus == ServiceOrderStatus.AWAITING_APPROVAL) {
            estimateServiceOrderAmountUseCase.estimate(serviceOrder.getId());
            if (finalizeInspectionUseCase != null) {
                finalizeInspectionUseCase.finalizeInspection(serviceOrder.getId());
            }
        }
    }

    private boolean isServiceAction(ServiceOrderAction action) {
        return action == ServiceOrderAction.START_SERVICE
                || action == ServiceOrderAction.COMPLETE_SERVICE
                || action == ServiceOrderAction.CANCEL_SERVICE;
    }

    private void handleServiceAction(ServiceOrderAction action, Long relatedServiceId, ServiceOrder serviceOrder) {
        switch (action) {
            case START_SERVICE -> {
                changeServiceStatusUseCase.startService(serviceOrder.getId(), relatedServiceId);
                updateServiceOrderIfNeeded(serviceOrder, ServiceOrderStatus.IN_PROGRESS);
            }
            case COMPLETE_SERVICE -> {
                changeServiceStatusUseCase.completeService(serviceOrder.getId(), relatedServiceId);
                checkAndUpdateToCompleted(serviceOrder);
            }
            case CANCEL_SERVICE -> {
                changeServiceStatusUseCase.cancelService(serviceOrder.getId(), relatedServiceId);
                applyStatusTransition(serviceOrder, ServiceOrderStatus.CANCELLED);
            }
            default -> {
                var targetStatus = resolveProgressTarget(action);
                applyStatusTransition(serviceOrder, targetStatus);
            }
        }
    }

    private void updateServiceOrderIfNeeded(ServiceOrder serviceOrder, ServiceOrderStatus targetStatus) {
        if (!"IN_PROGRESS".equals(serviceOrder.getStatus())) {
            applyStatusTransition(serviceOrder, targetStatus);
        }
    }

    private void checkAndUpdateToCompleted(ServiceOrder serviceOrder) {
        final var services = servicePersistencePort.findAllByServiceOrderId(serviceOrder.getId());
        boolean allCompleted = services.stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()));

        if (allCompleted && !"COMPLETED".equals(serviceOrder.getStatus())) {
            applyStatusTransition(serviceOrder, ServiceOrderStatus.COMPLETED);
        }
    }

    public ServiceOrder registerClientDecision(Long id, BudgetDecision decision) {
        final var serviceOrder = serviceOrderPersistencePort.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));

        final var currentStatus = ServiceOrderStatus.valueOf(serviceOrder.getStatus());
        final var targetStatus = resolveBudgetTarget(decision);

        if (targetStatus == ServiceOrderStatus.PARTIALLY_REJECTED) {
            throw new PartialBudgetRejectionNotImplementedException();
        }

        validateTransition(currentStatus, targetStatus);
        applyStatusTransition(serviceOrder, targetStatus);

        reverberate(serviceOrder, targetStatus);

        var saved = serviceOrderPersistencePort.save(serviceOrder);
        saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        return saved;
    }

    private void validateTransition(ServiceOrderStatus currentStatus, ServiceOrderStatus targetStatus) {
        ServiceOrderState.of(currentStatus).transitionTo(targetStatus);
    }

    private ServiceOrderStatus resolveProgressTarget(ServiceOrderAction action) {
        return switch (action) {
            case START_INSPECTION -> ServiceOrderStatus.IN_INSPECTION;
            case COMPLETE_INSPECTION -> ServiceOrderStatus.AWAITING_APPROVAL;
            case DELIVER_VEHICLE -> ServiceOrderStatus.DELIVERED;
            case START_SERVICE -> ServiceOrderStatus.IN_PROGRESS;
            case COMPLETE_SERVICE -> ServiceOrderStatus.COMPLETED;
            case CANCEL_SERVICE -> ServiceOrderStatus.CANCELLED;
        };
    }

    private ServiceOrderStatus resolveBudgetTarget(BudgetDecision decision) {
        return switch (decision) {
            case APPROVE -> ServiceOrderStatus.APPROVED;
            case CANCEL, REJECT -> ServiceOrderStatus.CANCELLED;
            case PARTIALLY_REJECT -> ServiceOrderStatus.PARTIALLY_REJECTED;
        };
    }

    private void applyStatusTransition(ServiceOrder serviceOrder, ServiceOrderStatus targetStatus) {
        final var now = LocalDateTime.now();
        serviceOrder.setStatus(targetStatus.name());
        serviceOrder.setStatusLabel(statusLabelPort.resolve(targetStatus.name()));
        serviceOrder.setUpdatedAt(now);

        switch (targetStatus) {
            case IN_INSPECTION -> serviceOrder.setInspectedAt(now);
            case APPROVED -> serviceOrder.setApprovedAt(now);
            case CANCELLED -> serviceOrder.setCancelledAt(now);
            case IN_PROGRESS -> serviceOrder.setStartedAt(now);
            case COMPLETED -> serviceOrder.setCompletedAt(now);
            case DELIVERED -> serviceOrder.setDeliveredAt(now);
            case PARTIALLY_REJECTED -> serviceOrder.setPartiallyRejectedAt(now);
            default -> {
            }
        }
    }

    /**
     * Reverberates status changes to related services based on OS decision.
     * - APPROVED: services in AWAITING_APPROVAL -> APPROVED
     * - CANCELLED: services in AWAITING_APPROVAL -> CANCELLED
     */
    private void reverberate(ServiceOrder serviceOrder, ServiceOrderStatus targetStatus) {
        if (targetStatus == ServiceOrderStatus.APPROVED || targetStatus == ServiceOrderStatus.CANCELLED) {
            final var services = servicePersistencePort.findAllByServiceOrderId(serviceOrder.getId());
            final var now = LocalDateTime.now();

            for (Service service : services) {
                if ("AWAITING_APPROVAL".equals(service.getStatus())) {
                    service.setStatus(targetStatus.name());
                    service.setUpdatedAt(now);

                    if (targetStatus == ServiceOrderStatus.APPROVED) {
                        service.setApprovedAt(now);
                    } else if (targetStatus == ServiceOrderStatus.CANCELLED) {
                        service.setCancelledAt(now);
                    }

                    servicePersistencePort.save(service);
                }
            }
        }
    }
}
