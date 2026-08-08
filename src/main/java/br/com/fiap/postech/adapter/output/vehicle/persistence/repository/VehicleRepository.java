package br.com.fiap.postech.adapter.output.vehicle.persistence.repository;

import br.com.fiap.postech.adapter.output.vehicle.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long>, JpaSpecificationExecutor<VehicleEntity> {
    Optional<VehicleEntity> findByLicensePlate(String licensePlate);
}
