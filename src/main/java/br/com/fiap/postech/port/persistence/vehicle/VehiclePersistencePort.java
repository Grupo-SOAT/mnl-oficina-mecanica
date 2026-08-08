package br.com.fiap.postech.port.persistence.vehicle;

import java.util.Optional;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;

public interface VehiclePersistencePort {

    ScrollPage<Vehicle> scroll(String licensePlate, Integer pageSize, String cursor);

    Optional<Vehicle> findById(Long id);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    Vehicle save(Vehicle vehicle);

    void deleteById(Long id);

    boolean existsById(Long id);
}
