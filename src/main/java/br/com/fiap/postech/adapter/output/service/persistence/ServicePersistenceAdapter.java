package br.com.fiap.postech.adapter.output.service.persistence;

import br.com.fiap.postech.adapter.output.catalogservice.persistence.repository.CatalogServicesRepository;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.Scroller;
import br.com.fiap.postech.adapter.output.service.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.adapter.output.service.persistence.repository.ServiceRepository;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ServicePersistenceAdapter implements ServicePersistencePort {

    private final ServiceRepository repository;
    private final CatalogServicesRepository catalogServicesRepository;

    @Override
    public ScrollPage<Service> scroll(Long serviceOrderId, Long serviceId, String status, Integer pageSize, String cursor) {
        return Scroller.scroll(
                cursor,
                pageSize,
                (parsedCursor, pageable) -> repository.findAll(
                                buildSpecification(serviceOrderId, serviceId, status, parsedCursor),
                                pageable
                        )
                        .getContent()
                        .stream()
                        .map(item -> (Service) item)
                        .toList()
        );
    }

    private Specification<ServiceEntity> buildSpecification(Long serviceOrderId, Long serviceId, String status, Long cursor) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            query.orderBy(criteriaBuilder.asc(root.get("id")));

            final var predicates = new ArrayList<Predicate>();

            if (serviceOrderId != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceOrderId"), serviceOrderId));
            }

            if (serviceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), serviceId));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (cursor > 0) {
                predicates.add(criteriaBuilder.greaterThan(root.get("id"), cursor));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Optional<Service> findByIdAndServiceOrderId(Long id, Long serviceOrderId) {
        return repository.findByIdAndServiceOrderId(id, serviceOrderId).map(item -> (Service) item);
    }

    @Override
    public List<Service> findAllByServiceOrderId(Long serviceOrderId) {
        return repository.findAllByServiceOrderId(serviceOrderId).stream()
                .map(item -> (Service) item)
                .toList();
    }

    @Override
    public Service save(Service service) {
        if (service instanceof ServiceEntity entity) {
            return repository.save(entity);
        }
        return repository.save(toEntity(service));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public ServiceCalculatedAverageTime calculateAverageTime(Long id) {
        List<ServiceEntity> services = repository.findByCatalogServiceId(id);
        if (services.isEmpty()) {
            return null;
        }

        return toCalculatedAverageTime(id, services, loadCatalogNamesById(List.of(id)));
    }

    @Override
    public List<ServiceCalculatedAverageTime> calculateAverageTime() {
        Map<Long, List<ServiceEntity>> servicesByCatalogId = repository.findAll().stream()
                .collect(Collectors.groupingBy(ServiceEntity::getCatalogServiceId));
        Map<Long, String> serviceNamesById = loadCatalogNamesById(new ArrayList<>(servicesByCatalogId.keySet()));

        return servicesByCatalogId.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toCalculatedAverageTime(entry.getKey(), entry.getValue(), serviceNamesById))
                .collect(Collectors.toList());
    }

    private ServiceCalculatedAverageTime toCalculatedAverageTime(
            Long catalogServiceId,
            List<ServiceEntity> services,
            Map<Long, String> serviceNamesById
    ) {
        long totalCreated = services.size();
        long totalCompleted = services.stream().filter(s -> s.getCompletedAt() != null).count();
        double averageCreateToComplete = averageOrZero(services, ServiceEntity::getCreatedAt, ServiceEntity::getCompletedAt);
        double averageStartToComplete = averageOrZero(services, ServiceEntity::getStartedAt, ServiceEntity::getCompletedAt);
        double averageApproveToComplete = averageOrZero(services, ServiceEntity::getApprovedAt, ServiceEntity::getCompletedAt);
        double averageAwaitingApproval = averageOrZero(services, ServiceEntity::getCreatedAt, ServiceEntity::getApprovedAt);

        return ServiceCalculatedAverageTime.builder()
                .id(catalogServiceId)
                .name(serviceNamesById.getOrDefault(catalogServiceId, ""))
                .totalCreated(totalCreated)
                .totalCompleted(totalCompleted)
                .averageTimeBetweenCreateAndComplete(averageCreateToComplete)
                .averageTimeBetweenStartAndComplete(averageStartToComplete)
                .averageTimeBetweenApproveAndComplete(averageApproveToComplete)
                .averageTimeAwaitingBudgetApproval(averageAwaitingApproval)
                .build();
    }

    private Map<Long, String> loadCatalogNamesById(List<Long> catalogServiceIds) {
        Map<Long, String> namesById = new HashMap<>();
        catalogServicesRepository.findAllById(catalogServiceIds)
                .forEach(item -> namesById.put(item.getId(), item.getName()));
        return namesById;
    }

    private double averageOrZero(
            List<ServiceEntity> services,
            Function<ServiceEntity, LocalDateTime> startExtractor,
            Function<ServiceEntity, LocalDateTime> endExtractor
    ) {
        return services.stream()
                .map(service -> hoursBetweenIfPresent(startExtractor.apply(service), endExtractor.apply(service)))
                .flatMap(Optional::stream)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private Optional<Double> hoursBetweenIfPresent(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return Optional.empty();
        }
        return Optional.of(Duration.between(start, end).toMillis() / 3_600_000.0);
    }

    private ServiceEntity toEntity(Service service) {
        List<NeededSupplyEntity> supplyEntities = service.getNeededSupplies() == null
                ? new ArrayList<>()
                : service.getNeededSupplies().stream()
                .map(n -> NeededSupplyEntity.builder()
                        .idSupply(n.getIdSupply())
                        .note(n.getNote())
                        .quantity(n.getQuantity())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        return ServiceEntity.builder()
                .id(service.getId())
                .serviceOrderId(service.getServiceOrderId())
                .catalogServiceId(service.getCatalogServiceId())
                .price(service.getPrice())
                .neededSupplyEntities(supplyEntities)
                .status(service.getStatus())
                .statusLabel(service.getStatusLabel())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .rejectedAt(service.getRejectedAt())
                .cancelledAt(service.getCancelledAt())
                .approvedAt(service.getApprovedAt())
                .startedAt(service.getStartedAt())
                .completedAt(service.getCompletedAt())
                .build();
    }
}
