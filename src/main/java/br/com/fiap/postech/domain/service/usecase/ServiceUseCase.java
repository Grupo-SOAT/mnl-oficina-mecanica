package br.com.fiap.postech.domain.service.usecase;

import br.com.fiap.postech.adapter.output.service.persistence.entity.NeededSupplyEntity;
import br.com.fiap.postech.adapter.output.service.persistence.entity.ServiceEntity;
import br.com.fiap.postech.domain.catalogservices.exception.CatalogServiceNotFoundException;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.service.exception.NoMatchingServicesException;
import br.com.fiap.postech.domain.service.exception.ServiceNotFoundException;
import br.com.fiap.postech.domain.service.exception.reason.ServiceExceptionReason;
import br.com.fiap.postech.domain.service.model.NeededSupply;
import br.com.fiap.postech.domain.service.model.Service;

import java.util.List;

import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;

import java.math.BigDecimal;

public class ServiceUseCase {
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ServicePersistencePort persistencePort;
    private final ServiceOrderPersistencePort serviceOrderPersistencePort;
    private final CatalogServicesPersistencePort catalogServicesPersistencePort;
    private final SupplyPersistencePort supplyPersistencePort;
    private final ServiceStatusLabelPort statusLabelPort;

    public ServiceUseCase(
            ServicePersistencePort persistencePort,
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            CatalogServicesPersistencePort catalogServicesPersistencePort,
            SupplyPersistencePort supplyPersistencePort,
            ServiceStatusLabelPort statusLabelPort
    ) {
        this.persistencePort = persistencePort;
        this.serviceOrderPersistencePort = serviceOrderPersistencePort;
        this.catalogServicesPersistencePort = catalogServicesPersistencePort;
        this.supplyPersistencePort = supplyPersistencePort;
        this.statusLabelPort = statusLabelPort;
    }

    public ScrollPage<Service> scroll(Long serviceOrderId, Long serviceId, String status, Integer pageSize, String cursor) {
        ensureServiceOrderExists(serviceOrderId);
        final var result = persistencePort.scroll(serviceOrderId, serviceId, status, pageSize, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingServicesException(serviceOrderId);
        }

        result.data().forEach(s -> s.setStatusLabel(statusLabelPort.resolve(s.getStatus())));
        return result;
    }

    public Service getById(Long serviceOrderId, Long serviceId) {
        ensureServiceOrderExists(serviceOrderId);
        var service = persistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        service.setStatusLabel(statusLabelPort.resolve(service.getStatus()));
        return service;
    }

    public Service create(Long serviceOrderId, Service service) {
        ensureServiceOrderExists(serviceOrderId);
        validateService(service);
        service.setServiceOrderId(serviceOrderId);
        service.setStatus("AWAITING_APPROVAL");
        var saved = persistencePort.save(service);
        saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        return saved;
    }

    public Service createFromCatalog(Long serviceOrderId, Long catalogServiceId) {
        ensureServiceOrderExists(serviceOrderId);
        var catalog = catalogServicesPersistencePort.findById(catalogServiceId)
                .orElseThrow(() -> new CatalogServiceNotFoundException(catalogServiceId));

        List<NeededSupplyEntity> supplies = catalog.getSupplies().stream()
                .map(s -> NeededSupplyEntity.builder()
                        .idSupply(s.getSupply().getId())
                        .quantity(s.getSupplyAmount())
                        .note(null)
                        .build()
                ).toList();

        var service = ServiceEntity.builder()
                .catalogServiceId(catalogServiceId)
                .price(catalog.getBasePrice())
                .neededSupplyEntities(supplies)
                .build();

        return this.create(serviceOrderId, service);
    }

    public Service update(Long serviceOrderId, Long serviceId, Service service) {
        ensureServiceOrderExists(serviceOrderId);
        final var existing = persistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        validateService(service);
        service.setId(serviceId);
        service.setServiceOrderId(serviceOrderId);
        // IMPORTANTE: preservar status existente - mudanças de status SÓ via endpoints de progresso
        service.setStatus(existing.getStatus());
        var saved = persistencePort.save(service);
        saved.setStatusLabel(statusLabelPort.resolve(saved.getStatus()));
        return saved;
    }

    public void delete(Long serviceOrderId, Long serviceId) {
        ensureServiceOrderExists(serviceOrderId);
        persistencePort.findByIdAndServiceOrderId(serviceId, serviceOrderId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));

        persistencePort.deleteById(serviceId);
    }

    private void ensureServiceOrderExists(Long serviceOrderId) {
        if (serviceOrderPersistencePort.findById(serviceOrderId).isEmpty()) {
            throw new ServiceOrderNotFoundException(serviceOrderId);
        }
    }

    private void validateService(Service service) {
        if (service.getCatalogServiceId() == null || catalogServicesPersistencePort.findById(service.getCatalogServiceId()).isEmpty()) {
            throw new CatalogServiceNotFoundException(service.getCatalogServiceId());
        }

        if (service.getPrice() == null || service.getPrice().compareTo(ZERO) < 0) {
            throw new InvalidServiceException(ServiceExceptionReason.INVALID_PRICE);
        }

        List<NeededSupply> neededSupplies = service.getNeededSupplies();
        if (neededSupplies == null) {
            return;
        }

        for (NeededSupply neededSupply : neededSupplies) {
            if (neededSupply.getQuantity() == null || neededSupply.getQuantity() <= 0) {
                throw new InvalidServiceException(ServiceExceptionReason.INVALID_NEEDED_SUPPLY_QUANTITY);
            }
            if (neededSupply.getIdSupply() == null || supplyPersistencePort.findById(neededSupply.getIdSupply()).isEmpty()) {
                throw new InvalidServiceException(ServiceExceptionReason.NEEDED_SUPPLY_NOT_FOUND);
            }
        }
    }

    public static class InvalidServiceException extends RuntimeException {
        public final ServiceExceptionReason reason;

        public InvalidServiceException(ServiceExceptionReason reason) {
            super(reason.name());
            this.reason = reason;
        }
    }
}
