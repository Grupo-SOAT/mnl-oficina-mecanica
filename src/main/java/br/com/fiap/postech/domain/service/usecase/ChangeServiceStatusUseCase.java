package br.com.fiap.postech.domain.service.usecase;

import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus.*;

public class ChangeServiceStatusUseCase {

    private final ServicePersistencePort servicePersistencePort;
    private final SupplyPersistencePort supplyPersistencePort;
    private final ServiceStatusLabelPort statusLabelPort;

    public ChangeServiceStatusUseCase(
            ServicePersistencePort servicePersistencePort,
            SupplyPersistencePort supplyPersistencePort,
            ServiceStatusLabelPort statusLabelPort
    ) {
        this.servicePersistencePort = servicePersistencePort;
        this.supplyPersistencePort = supplyPersistencePort;
        this.statusLabelPort = statusLabelPort;
    }

    /**
     * Handle START_SERVICE action: mark service as IN_PROGRESS, decrement reserved supplies, and
     * notify the caller to transition the OS to IN_PROGRESS when this is the first service started.
     */
    public Service startService(
            Long serviceOrderId,
            Long serviceId,
            Consumer<ServiceOrderStatus> onServiceOrderTransition
    ) {
        final var service = servicePersistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        final var now = LocalDateTime.now();
        service.setStatus(IN_PROGRESS.name());
        service.setStatusLabel(statusLabelPort.resolve(IN_PROGRESS.name()));
        service.setStartedAt(now);
        service.setUpdatedAt(now);

        if (service.getNeededSupplies() != null) {
            for (var needed : service.getNeededSupplies()) {
                final var supply = supplyPersistencePort.findById(needed.getIdSupply())
                        .orElseThrow(() -> new ServiceNotFoundException(serviceId));

                final var newReserved = supply.getReservedQuantity() - needed.getQuantity();
                if (newReserved < 0) {
                    throw new NegativeSupplyQuantityException(needed.getIdSupply());
                }

                supply.setReservedQuantity(newReserved);
                supplyPersistencePort.save(supply);
            }
        }

        var savedService = servicePersistencePort.save(service);
        savedService.setStatusLabel(statusLabelPort.resolve(savedService.getStatus()));

        updateServiceOrderIfFirstServiceStarted(serviceOrderId, serviceId, onServiceOrderTransition);

        return savedService;
    }

    /**
     * Handle COMPLETE_SERVICE action: mark service as COMPLETED and notify the caller to transition
     * the OS to COMPLETED when all services are done.
     */
    public Service completeService(
            Long serviceOrderId,
            Long serviceId,
            Consumer<ServiceOrderStatus> onServiceOrderTransition
    ) {
        final var service = servicePersistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        final var now = LocalDateTime.now();
        service.setStatus(COMPLETED.name());
        service.setCompletedAt(now);
        service.setUpdatedAt(now);

        var savedService = servicePersistencePort.save(service);
        savedService.setStatusLabel(statusLabelPort.resolve(savedService.getStatus()));

        updateServiceOrderIfLastServiceCompleted(serviceOrderId, onServiceOrderTransition);

        return savedService;
    }

    /**
     * Handle CANCEL_SERVICE action: mark service as CANCELLED and release reserved supplies.
     */
    public Service cancelService(Long serviceOrderId, Long serviceId) {
        final var service = servicePersistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        final var now = LocalDateTime.now();
        service.setStatus(CANCELLED.name());
        service.setCancelledAt(now);
        service.setUpdatedAt(now);

        if (service.getNeededSupplies() != null) {
            for (var needed : service.getNeededSupplies()) {
                final var supply = supplyPersistencePort.findById(needed.getIdSupply())
                        .orElseThrow(() -> new ServiceNotFoundException(serviceId)); // Shouldn't happen

                final var newReserved = supply.getReservedQuantity() - needed.getQuantity();
                final var newAvailable = supply.getAvailableQuantity() + needed.getQuantity();

                if (newReserved < 0) {
                    throw new NegativeSupplyQuantityException(needed.getIdSupply());
                }

                supply.setReservedQuantity(newReserved);
                supply.setAvailableQuantity(newAvailable);
                supplyPersistencePort.save(supply);
            }
        }

        var saved = servicePersistencePort.save(service);
        saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        return saved;
    }

    /**
     * Notifies the caller that the OS should move to IN_PROGRESS when the service being started is
     * the first one (no other service is already IN_PROGRESS).
     */
    private void updateServiceOrderIfFirstServiceStarted(
            Long serviceOrderId,
            Long currentServiceId,
            Consumer<ServiceOrderStatus> onTransition
    ) {
        final var services = servicePersistencePort.findAllByServiceOrderId(serviceOrderId);
        final var hasOtherInProgressService = services.stream()
                .filter(s -> !currentServiceId.equals(s.getId()))
                .anyMatch(s -> IN_PROGRESS.name().equals(s.getStatus()));

        if (!hasOtherInProgressService) {
            onTransition.accept(IN_PROGRESS);
        }
    }

    /**
     * Notifies the caller that the OS should move to COMPLETED when all services are done
     * (COMPLETED or CANCELLED) and none are IN_PROGRESS/APPROVED.
     */
    private void updateServiceOrderIfLastServiceCompleted(
            Long serviceOrderId,
            Consumer<ServiceOrderStatus> onTransition
    ) {
        final var services = servicePersistencePort.findAllByServiceOrderId(serviceOrderId);

        // Check if all services are completed or cancelled
        final var allDone = services.stream()
                .allMatch(s -> COMPLETED.name().equals(s.getStatus()) || CANCELLED.name().equals(s.getStatus()));

        // Check if any service is still IN_PROGRESS or APPROVED
        final var anyInProgress = services.stream()
                .anyMatch(s -> IN_PROGRESS.name().equals(s.getStatus()) || APPROVED.name().equals(s.getStatus()));

        if (allDone && !anyInProgress) {
            onTransition.accept(COMPLETED);
        }
    }
}
