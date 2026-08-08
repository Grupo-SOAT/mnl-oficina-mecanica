package br.com.fiap.postech.domain.vehicle.usecase;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import br.com.fiap.postech.domain.vehicle.excecption.*;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.domain.vehicle.validation.VehicleLicensePlateValidator;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;

public class VehicleUseCase {
    private final VehiclePersistencePort persistencePort;
    private final OwnerPersistencePort ownerPersistencePort;

    public VehicleUseCase(
            VehiclePersistencePort persistencePort,
            OwnerPersistencePort ownerPersistencePort
    ) {
        this.persistencePort = persistencePort;
        this.ownerPersistencePort = ownerPersistencePort;
    }

    public ScrollPage<Vehicle> scroll(String licensePlate, Integer pageSize, String cursor) {
        final var result = persistencePort.scroll(licensePlate, pageSize, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingVehiclesException(licensePlate);
        }

        return result;
    }

    public Vehicle getById(Long id) {
        return persistencePort.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    public Vehicle create(Vehicle vehicle) {
        validateLicensePlate(vehicle);
        validateYear(vehicle);
        validateOwnerExists(vehicle.getOwnerId());

        persistencePort.findByLicensePlate(vehicle.getLicensePlate()).ifPresent(s -> {
            throw new DuplicatedVehicleException(vehicle.getLicensePlate());
        });
        return persistencePort.save(vehicle);
    }

    public Vehicle update(Long id, Vehicle vehicle) {
        validateLicensePlate(vehicle);
        validateYear(vehicle);
        validateOwnerExists(vehicle.getOwnerId());

        persistencePort.findByLicensePlate(vehicle.getLicensePlate())
                .ifPresent(existingVehicle -> {
                    if (!existingVehicle.getId().equals(id)) {
                        throw new DuplicatedVehicleException(vehicle.getLicensePlate());
                    }
                });

        persistencePort.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
        vehicle.setId(id);

        return persistencePort.save(vehicle);
    }

    public void delete(Long id) {
        if (!persistencePort.existsById(id)) {
            throw new VehicleNotFoundException(id);
        }

        persistencePort.deleteById(id);
    }

    private void validateLicensePlate(Vehicle vehicle) {

        if (!VehicleLicensePlateValidator.isValid(vehicle.getLicensePlate())) {
            throw new InvalidLicensePlateException(vehicle.getLicensePlate());
        }
    }

    private void validateYear(Vehicle vehicle) {
        Integer year = vehicle.getYear();

        if (year == null || year < 1900 || year > 2050) {
            throw new InvalidVehicleYearException(year);
        }
    }

    private void validateOwnerExists(Long ownerId) {
        ownerPersistencePort.findById(ownerId)
                .orElseThrow(() ->
                        new OwnerNotFoundException(ownerId)
                );
    }

}
