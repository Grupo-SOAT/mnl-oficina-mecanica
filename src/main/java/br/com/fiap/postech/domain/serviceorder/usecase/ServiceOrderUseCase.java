package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.serviceorder.exception.NoMatchingServiceOrdersException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderClientNotFoundException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderVehicleNotFoundException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;

public class ServiceOrderUseCase {

    private final ServiceOrderPersistencePort persistencePort;
    private final OwnerPersistencePort ownerPersistencePort;
    private final VehiclePersistencePort vehiclePersistencePort;
    private final ServiceOrderStatusLabelPort statusLabelPort;

    public ServiceOrderUseCase(
            ServiceOrderPersistencePort persistencePort,
            OwnerPersistencePort ownerPersistencePort,
            VehiclePersistencePort vehiclePersistencePort,
            ServiceOrderStatusLabelPort statusLabelPort
    ) {
        this.persistencePort = persistencePort;
        this.ownerPersistencePort = ownerPersistencePort;
        this.vehiclePersistencePort = vehiclePersistencePort;
        this.statusLabelPort = statusLabelPort;
    }

    public ScrollPage<ServiceOrder> scroll(String status, Long clientId, Long vehicleId, Integer pageSize, String cursor) {
        final var result = persistencePort.scroll(status, clientId, vehicleId, pageSize, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingServiceOrdersException(status != null ? "status=" + status : "all");
        }

        result.data().forEach(so -> so.setStatusLabel(statusLabelPort.resolve(so.getStatus())));
        return result;
    }

    public ServiceOrder getById(Long id) {
        var serviceOrder = persistencePort.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));
        serviceOrder.setStatusLabel(statusLabelPort.resolve(serviceOrder.getStatus()));
        return serviceOrder;
    }

    public ServiceOrder create(ServiceOrder serviceOrder) {
        validateOwnerExists(serviceOrder.getClientId());
        validateVehicleExists(serviceOrder.getVehicleId());

        serviceOrder.setStatus("PENDING");
        var saved = persistencePort.save(serviceOrder);
        saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        return saved;
    }

    public ServiceOrder update(Long id, ServiceOrder serviceOrder) {
        validateOwnerExists(serviceOrder.getClientId());
        validateVehicleExists(serviceOrder.getVehicleId());

        persistencePort.findById(id)
                .orElseThrow(() -> new ServiceOrderNotFoundException(id));

        serviceOrder.setId(id);
        var saved = persistencePort.save(serviceOrder);
        if (saved.getStatus() != null) {
            saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        }
        return saved;
    }

    public void delete(Long id) {
        if (!persistencePort.existsById(id)) {
            throw new ServiceOrderNotFoundException(id);
        }

        persistencePort.deleteById(id);
    }

    private void validateOwnerExists(Long ownerId) {
        ownerPersistencePort.findById(ownerId)
                .orElseThrow(() -> new ServiceOrderClientNotFoundException(ownerId));
    }

    private void validateVehicleExists(Long vehicleId) {
        vehiclePersistencePort.findById(vehicleId)
                .orElseThrow(() -> new ServiceOrderVehicleNotFoundException(vehicleId));
    }
}
