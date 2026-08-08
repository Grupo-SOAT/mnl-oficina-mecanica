package br.com.fiap.postech.domain.service.usecase;

import br.com.fiap.postech.domain.service.exception.NegativeSupplyQuantityException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;

import java.time.LocalDateTime;

public class ChangeServiceStatusUseCase {

    private final ServicePersistencePort servicePersistencePort;
    private final ServiceOrderPersistencePort serviceOrderPersistencePort;
    private final SupplyPersistencePort supplyPersistencePort;
    private final ServiceStatusLabelPort statusLabelPort;

    public ChangeServiceStatusUseCase(
            ServicePersistencePort servicePersistencePort,
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            SupplyPersistencePort supplyPersistencePort,
            ServiceStatusLabelPort statusLabelPort
    ) {
        this.servicePersistencePort = servicePersistencePort;
        this.serviceOrderPersistencePort = serviceOrderPersistencePort;
        this.supplyPersistencePort = supplyPersistencePort;
        this.statusLabelPort = statusLabelPort;
    }

    /**
     * Handle START_SERVICE action: mark service as IN_PROGRESS, update OS if first service,
     * and decrement reserved supplies accordingly.
     */
    public Service startService(Long serviceOrderId, Long serviceId) {
        final var service = servicePersistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        final var now = LocalDateTime.now();
        service.setStatus("IN_PROGRESS");
        service.setStatusLabel(statusLabelPort.resolve("IN_PROGRESS"));
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

        updateServiceOrderIfFirstServiceStarted(serviceOrderId, serviceId);

        return savedService;
    }

    /**
     * Handle COMPLETE_SERVICE action: mark service as COMPLETED and update OS if last service.
     */
    public Service completeService(Long serviceOrderId, Long serviceId) {
        final var service = servicePersistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        final var now = LocalDateTime.now();
        service.setStatus("COMPLETED");
        service.setCompletedAt(now);
        service.setUpdatedAt(now);

        var savedService = servicePersistencePort.save(service);
        savedService.setStatusLabel(statusLabelPort.resolve(savedService.getStatus()));

        updateServiceOrderIfLastServiceCompleted(serviceOrderId);

        return savedService;
    }

    /**
     * Handle CANCEL_SERVICE action: mark service as CANCELLED and release reserved supplies.
     */
    public Service cancelService(Long serviceOrderId, Long serviceId) {
        final var service = servicePersistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        final var now = LocalDateTime.now();
        service.setStatus("CANCELLED");
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

    private void updateServiceOrderIfFirstServiceStarted(Long serviceOrderId, Long currentServiceId) {
        final var serviceOrder = serviceOrderPersistencePort.findById(serviceOrderId).orElse(null);
        if (serviceOrder == null || !serviceOrder.getStatus().equals("APPROVED")) {
            return; // OS must be in APPROVED status for service to start
        }

        // Check if any other service is already IN_PROGRESS (excluding current one)
        final var services = servicePersistencePort.findAllByServiceOrderId(serviceOrderId);
        final var hasOtherInProgressService = services.stream()
                .filter(s -> !currentServiceId.equals(s.getId()))
                .anyMatch(s -> "IN_PROGRESS".equals(s.getStatus()));

        if (!hasOtherInProgressService) {
            serviceOrder.setStatus("IN_PROGRESS");
            serviceOrder.setStartedAt(LocalDateTime.now());
            serviceOrder.setUpdatedAt(LocalDateTime.now());
            serviceOrderPersistencePort.save(serviceOrder);
        }
    }

    /**
     * Update ServiceOrder to COMPLETED if all services are done and none are IN_PROGRESS/APPROVED.
     */
    private void updateServiceOrderIfLastServiceCompleted(Long serviceOrderId) {
        final var serviceOrder = serviceOrderPersistencePort.findById(serviceOrderId).orElse(null);
        if (serviceOrder == null || !serviceOrder.getStatus().equals("IN_PROGRESS")) {
            return;
        }

        final var services = servicePersistencePort.findAllByServiceOrderId(serviceOrderId);

        // Check if all services are completed or cancelled
        final var allDone = services.stream()
                .allMatch(s -> "COMPLETED".equals(s.getStatus()) || "CANCELLED".equals(s.getStatus()));

        // Check if any service is still IN_PROGRESS or APPROVED
        final var anyInProgress = services.stream()
                .anyMatch(s -> "IN_PROGRESS".equals(s.getStatus()) || "APPROVED".equals(s.getStatus()));

        if (allDone && !anyInProgress) {
            serviceOrder.setStatus("COMPLETED");
            serviceOrder.setCompletedAt(LocalDateTime.now());
            serviceOrder.setUpdatedAt(LocalDateTime.now());
            serviceOrderPersistencePort.save(serviceOrder);
        }
    }
}
