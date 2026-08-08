package br.com.fiap.postech.adapter.output.serviceorder.persistence;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.Scroller;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.repository.ServiceOrderRepository;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderStatus;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ServiceOrderPersistenceAdapter implements ServiceOrderPersistencePort {

    private static final List<ServiceOrderStatus> DEFAULT_LISTING_PRIORITY_ORDER = ServiceOrderStatus.defaultListingPriorityOrder();
    private static final List<String> EXCLUDED_FROM_DEFAULT_LISTING_NAMES = ServiceOrderStatus.EXCLUDED_FROM_DEFAULT_LISTING.stream()
            .map(Enum::name)
            .toList();

    private final ServiceOrderRepository repository;

    @Override
    public ScrollPage<ServiceOrder> scroll(String status, Long clientId, Long vehicleId, Integer pageSize, String cursor) {
        return Scroller.scroll(
                cursor,
                pageSize,
                (parsedCursor, pageable) -> repository.findAll(buildSpecification(status, clientId, vehicleId, parsedCursor), pageable)
                        .getContent()
                        .stream()
                        .map(item -> (ServiceOrder) item)
                        .toList()
        );
    }

    private Specification<ServiceOrderEntity> buildSpecification(String status, Long clientId, Long vehicleId, Long cursor) {
        final var hasStatusFilter = status != null && !status.isBlank();

        return (root, query, criteriaBuilder) -> {
            final var predicates = new ArrayList<Predicate>();

            if (hasStatusFilter) {
                query.orderBy(criteriaBuilder.asc(root.get("id")));
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
                if (cursor > 0) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("id"), cursor));
                }
            } else {
                final var priorityExpression = statusPriorityExpression(criteriaBuilder, root.<String>get("status"));
                query.orderBy(criteriaBuilder.asc(priorityExpression), criteriaBuilder.asc(root.get("id")));

                predicates.add(criteriaBuilder.not(root.<String>get("status").in(EXCLUDED_FROM_DEFAULT_LISTING_NAMES)));
                addDefaultListingCursorPredicate(predicates, criteriaBuilder, priorityExpression, root.get("id"), cursor);
            }

            if (clientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("clientId"), clientId));
            }

            if (vehicleId != null) {
                predicates.add(criteriaBuilder.equal(root.get("vehicleId"), vehicleId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Resumes the default listing (priority, id) order. Looks up the cursor row's own status to know which
     * priority bucket it belongs to, since a plain "id > cursor" predicate cannot express a composite order.
     */
    private void addDefaultListingCursorPredicate(
            List<Predicate> predicates,
            CriteriaBuilder criteriaBuilder,
            Expression<Integer> priorityExpression,
            Path<Long> idPath,
            Long cursor
    ) {
        if (cursor <= 0) {
            return;
        }

        repository.findById(cursor).ifPresentOrElse(
                cursorEntity -> {
                    final var cursorPriority = listingPriorityOf(cursorEntity.getStatus());
                    predicates.add(criteriaBuilder.or(
                            criteriaBuilder.greaterThan(priorityExpression, cursorPriority),
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(priorityExpression, cursorPriority),
                                    criteriaBuilder.greaterThan(idPath, cursor)
                            )
                    ));
                },
                () -> predicates.add(criteriaBuilder.greaterThan(idPath, cursor))
        );
    }

    private Expression<Integer> statusPriorityExpression(CriteriaBuilder criteriaBuilder, Path<String> statusPath) {
        var caseExpression = criteriaBuilder.<String, Integer>selectCase(statusPath);
        for (int i = 0; i < DEFAULT_LISTING_PRIORITY_ORDER.size(); i++) {
            caseExpression = caseExpression.when(DEFAULT_LISTING_PRIORITY_ORDER.get(i).name(), i);
        }
        return caseExpression.otherwise(DEFAULT_LISTING_PRIORITY_ORDER.size());
    }

    private int listingPriorityOf(String status) {
        try {
            final var index = DEFAULT_LISTING_PRIORITY_ORDER.indexOf(ServiceOrderStatus.valueOf(status));
            return index >= 0 ? index : DEFAULT_LISTING_PRIORITY_ORDER.size();
        } catch (IllegalArgumentException e) {
            return DEFAULT_LISTING_PRIORITY_ORDER.size();
        }
    }

    @Override
    public Optional<ServiceOrder> findById(Long id) {
        return repository.findById(id).map(item -> (ServiceOrder) item);
    }

    @Override
    public ServiceOrder save(ServiceOrder serviceOrder) {
        if (serviceOrder instanceof ServiceOrderEntity entity) {
            return repository.save(entity);
        }
        return repository.save(toEntity(serviceOrder));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    private ServiceOrderEntity toEntity(ServiceOrder serviceOrder) {
        return ServiceOrderEntity.builder()
                .id(serviceOrder.getId())
                .clientId(serviceOrder.getClientId())
                .vehicleId(serviceOrder.getVehicleId())
                .description(serviceOrder.getDescription())
                .status(serviceOrder.getStatus())
                .statusLabel(serviceOrder.getStatusLabel())
                .estimatedAmount(serviceOrder.getEstimatedAmount())
                .createdAt(serviceOrder.getCreatedAt())
                .updatedAt(serviceOrder.getUpdatedAt())
                .inspectedAt(serviceOrder.getInspectedAt())
                .approvedAt(serviceOrder.getApprovedAt())
                .rejectedAt(serviceOrder.getRejectedAt())
                .cancelledAt(serviceOrder.getCancelledAt())
                .startedAt(serviceOrder.getStartedAt())
                .completedAt(serviceOrder.getCompletedAt())
                .deliveredAt(serviceOrder.getDeliveredAt())
                .partiallyRejectedAt(serviceOrder.getPartiallyRejectedAt())
                .build();
    }
}
