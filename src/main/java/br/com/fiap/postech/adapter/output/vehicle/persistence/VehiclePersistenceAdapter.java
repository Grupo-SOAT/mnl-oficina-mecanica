package br.com.fiap.postech.adapter.output.vehicle.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.Scroller;
import br.com.fiap.postech.adapter.output.vehicle.persistence.entity.VehicleEntity;
import br.com.fiap.postech.adapter.output.vehicle.persistence.repository.VehicleRepository;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VehiclePersistenceAdapter implements VehiclePersistencePort{
    private final VehicleRepository repository;

    @Override
    public ScrollPage<Vehicle> scroll(String licensePlate, Integer pageSize, String cursor) {
        return Scroller.scroll(
                cursor,
                pageSize,
                (parsedCursor, pageable) -> repository.findAll(buildSpecification(licensePlate, parsedCursor), pageable)
                        .getContent()
                        .stream()
                        .map(item -> (Vehicle) item)
                        .toList()
        );
    }

    private Specification<VehicleEntity> buildSpecification(String licensePlate, Long cursor) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            query.orderBy(criteriaBuilder.asc(root.get("id")));

            final var predicates = new ArrayList<Predicate>();

            if (licensePlate != null && !licensePlate.isBlank()) {
                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("licensePlate")),
                                licensePlate.toLowerCase() + "%"
                        )
                );
            }

            if (cursor > 0) {
                predicates.add(criteriaBuilder.greaterThan(root.get("id"), cursor));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return repository.findById(id).map(item -> (Vehicle) item);
    }

    @Override
    public Optional<Vehicle> findByLicensePlate(String licensePlate) {
        return repository.findByLicensePlate(licensePlate).map(item -> (Vehicle) item);
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity;
        if (vehicle instanceof VehicleEntity vehicleEntity) {
            entity = vehicleEntity;
        } else {
            entity = new VehicleEntity();
            entity.setId(vehicle.getId());
            entity.setOwnerId(vehicle.getOwnerId());
            entity.setLicensePlate(vehicle.getLicensePlate());
            entity.setBrand(vehicle.getBrand());
            entity.setModel(vehicle.getModel());
            entity.setYear(vehicle.getYear());
            entity.setColor(vehicle.getColor());
        }

        return repository.save(entity);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }
}
