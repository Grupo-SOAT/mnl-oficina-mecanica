package br.com.fiap.postech.adapter.input.serviceorder.mapper;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.input.vehicle.mapper.VehicleMapper;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderCascadeCreationCommand;
import org.jspecify.annotations.NonNull;

import java.time.ZoneOffset;
import java.util.ArrayList;

public class ServiceOrderMapper {

    public static ServiceOrder fromApiRequest(@NonNull ServiceOrderRequest request) {
        return ServiceOrderEntity.builder()
                .clientId(request.getClientId())
                .vehicleId(request.getVehicleId())
                .description(request.getDescription())
                .build();
    }

    public static ServiceOrderCascadeCreationCommand buildCascadeCreationCommand(
            @NonNull ServiceOrderCascadeRequest request
    ) {
        final var serviceOrder = ServiceOrderEntity.builder()
                .clientId(request.getOwnerId())
                .vehicleId(request.getVehicleId())
                .description(request.getDescription())
                .build();
        final var vehicleCascadeCreationCommand = (request.getVehicle() != null)
                ? VehicleMapper.buildCascadeCreationCommand(request.getVehicle())
                : null;
        final var catalogServiceIds = (request.getCatalogServiceIds() != null)
                ? request.getCatalogServiceIds()
                : new ArrayList<Long>();

        return new ServiceOrderCascadeCreationCommand(
                vehicleCascadeCreationCommand,
                serviceOrder,
                catalogServiceIds
        );
    }

    public static ServiceOrder fromApiData(@NonNull ServiceOrderData data) {
        return ServiceOrderEntity.builder()
                .id(data.getId())
                .clientId(data.getClientId())
                .vehicleId(data.getVehicleId())
                .description(data.getDescription())
                .status(data.getStatus() != null ? data.getStatus().getValue() : null)
                .estimatedAmount(data.getEstimatedAmount())
                .build();
    }

    public static ServiceOrderData toApiData(@NonNull ServiceOrder serviceOrder) {
        ServiceOrderData data = new ServiceOrderData()
                .id(serviceOrder.getId())
                .clientId(serviceOrder.getClientId())
                .vehicleId(serviceOrder.getVehicleId())
                .description(serviceOrder.getDescription())
                .estimatedAmount(serviceOrder.getEstimatedAmount())
                .createdAt(serviceOrder.getCreatedAt() != null ? serviceOrder.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(serviceOrder.getUpdatedAt() != null ? serviceOrder.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .inspectedAt(serviceOrder.getInspectedAt() != null ? serviceOrder.getInspectedAt().atOffset(ZoneOffset.UTC) : null)
                .approvedAt(serviceOrder.getApprovedAt() != null ? serviceOrder.getApprovedAt().atOffset(ZoneOffset.UTC) : null)
                .rejectedAt(serviceOrder.getRejectedAt() != null ? serviceOrder.getRejectedAt().atOffset(ZoneOffset.UTC) : null)
                .cancelledAt(serviceOrder.getCancelledAt() != null ? serviceOrder.getCancelledAt().atOffset(ZoneOffset.UTC) : null)
                .startedAt(serviceOrder.getStartedAt() != null ? serviceOrder.getStartedAt().atOffset(ZoneOffset.UTC) : null)
                .completedAt(serviceOrder.getCompletedAt() != null ? serviceOrder.getCompletedAt().atOffset(ZoneOffset.UTC) : null)
                .deliveredAt(serviceOrder.getDeliveredAt() != null ? serviceOrder.getDeliveredAt().atOffset(ZoneOffset.UTC) : null)
                .partiallyRejectedAt(serviceOrder.getPartiallyRejectedAt() != null ? serviceOrder.getPartiallyRejectedAt().atOffset(ZoneOffset.UTC) : null);

        if (serviceOrder.getStatus() != null) {
            data.setStatus(ServiceOrderStatus.fromValue(serviceOrder.getStatus()));
        }

        data.setStatusLabel(serviceOrder.getStatusLabel());

        return data;
    }

    public static PaginatedServiceOrderResponse toPaginatedResponse(@NonNull ScrollPage<ServiceOrder> page) {
        final var result = new PaginatedServiceOrderResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(ServiceOrderMapper.toApiData(item)));

        return result;
    }
}
