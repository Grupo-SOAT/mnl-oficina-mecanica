package br.com.fiap.postech.adapter.input.service.mapper;

import br.com.fiap.postech.adapter.input.api.model.NeededSupplyData;
import br.com.fiap.postech.adapter.input.api.model.PaginatedServiceResponse;
import br.com.fiap.postech.adapter.input.api.model.ServiceData;
import br.com.fiap.postech.adapter.input.api.model.ServiceRequest;
import br.com.fiap.postech.adapter.input.api.model.ServiceStatus;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.domain.service.model.NeededSupply;
import br.com.fiap.postech.domain.service.model.Service;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

public class ServiceMapper {

    public static Service fromApiRequest(@NonNull ServiceRequest request) {
        ServiceEntity entity = ServiceEntity.builder()
                .catalogServiceId(request.getCatalogServiceId())
                .price(request.getPrice())
                .neededSupplyEntities(Collections.emptyList())
                .build();

        if (request.getNeededSupplies() != null) {
            entity.setNeededSupplies(request.getNeededSupplies().stream()
                    .map(n -> NeededSupply.builder()
                            .idSupply(n.getIdSupply())
                            .note(n.getNote())
                            .quantity(n.getQuantity())
                            .build())
                    .toList());
        }

        return entity;
    }

    public static Service fromApiData(@NonNull ServiceData data) {
        ServiceEntity entity = ServiceEntity.builder()
                .id(data.getId())
                .catalogServiceId(data.getCatalogServiceId())
                .price(data.getPrice())
                // IMPORTANTE: status é ignorado em updates normais - alterações só via endpoints de progresso
                .status(null)
                .neededSupplyEntities(Collections.emptyList())
                .build();

        if (data.getNeededSupplies() != null) {
            entity.setNeededSupplies(data.getNeededSupplies().stream()
                    .map(n -> NeededSupply.builder()
                            .idSupply(n.getIdSupply())
                            .note(n.getNote())
                            .quantity(n.getQuantity())
                            .build())
                    .toList());
        }

        return entity;
    }

    public static ServiceData toApiData(@NonNull Service service) {
        ServiceData data = new ServiceData()
                .id(service.getId())
                .serviceOrderId(service.getServiceOrderId())
                .catalogServiceId(service.getCatalogServiceId())
                .price(service.getPrice())
                .createdAt(service.getCreatedAt() != null ? service.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(service.getUpdatedAt() != null ? service.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .rejectedAt(service.getRejectedAt() != null ? service.getRejectedAt().atOffset(ZoneOffset.UTC) : null)
                .cancelledAt(service.getCancelledAt() != null ? service.getCancelledAt().atOffset(ZoneOffset.UTC) : null)
                .approvedAt(service.getApprovedAt() != null ? service.getApprovedAt().atOffset(ZoneOffset.UTC) : null)
                .startedAt(service.getStartedAt() != null ? service.getStartedAt().atOffset(ZoneOffset.UTC) : null)
                .completedAt(service.getCompletedAt() != null ? service.getCompletedAt().atOffset(ZoneOffset.UTC) : null);

        if (service.getStatus() != null) {
            data.setStatus(ServiceStatus.fromValue(service.getStatus()));
        }

        data.setStatusLabel(service.getStatusLabel());

        List<NeededSupply> neededSupplies = service.getNeededSupplies();
        if (neededSupplies != null && !neededSupplies.isEmpty()) {
            data.setNeededSupplies(neededSupplies.stream()
                    .map(n -> new NeededSupplyData()
                            .idSupply(n.getIdSupply())
                            .note(n.getNote())
                            .quantity(n.getQuantity()))
                    .toList());
        }

        return data;
    }

    public static PaginatedServiceResponse toPaginatedResponse(@NonNull ScrollPage<Service> page) {
        final var result = new PaginatedServiceResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(ServiceMapper.toApiData(item)));

        return result;
    }
}
