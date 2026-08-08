package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.domain.service.model.NeededSupply;
import br.com.fiap.postech.domain.service.model.Service;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderNotFoundException;
import br.com.fiap.postech.domain.supply.model.Supply;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstimateServiceOrderAmountUseCase {

    private final ServiceOrderPersistencePort serviceOrderPersistencePort;
    private final ServicePersistencePort servicePersistencePort;
    private final SupplyPersistencePort supplyPersistencePort;

    public EstimateServiceOrderAmountUseCase(
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            ServicePersistencePort servicePersistencePort,
            SupplyPersistencePort supplyPersistencePort
    ) {
        this.serviceOrderPersistencePort = serviceOrderPersistencePort;
        this.servicePersistencePort = servicePersistencePort;
        this.supplyPersistencePort = supplyPersistencePort;
    }

    public void estimate(Long serviceOrderId) {
        var serviceOrder = serviceOrderPersistencePort.findById(serviceOrderId)
                .orElseThrow(() -> new ServiceOrderNotFoundException(serviceOrderId));

        var services = servicePersistencePort.findAllByServiceOrderId(serviceOrderId);
        if (services.isEmpty()) {
            serviceOrder.setEstimatedAmount(BigDecimal.ZERO);
            serviceOrderPersistencePort.save(serviceOrder);
            return;
        }

        List<Long> supplyIds = collectDistinctSupplyIds(services);
        Map<Long, BigDecimal> priceById = supplyIds.isEmpty()
                ? Map.of()
                : supplyPersistencePort.findAllById(supplyIds).stream()
                        .collect(Collectors.toMap(Supply::getId, Supply::getUnitPrice));

        BigDecimal total = BigDecimal.ZERO;
        for (Service svc : services) {
            if (svc.getPrice() != null) {
                total = total.add(svc.getPrice());
            }
            for (NeededSupply ns : svc.getNeededSupplies()) {
                BigDecimal unitPrice = priceById.get(ns.getIdSupply());
                if (unitPrice != null) {
                    total = total.add(unitPrice.multiply(BigDecimal.valueOf(ns.getQuantity())));
                }
            }
        }

        serviceOrder.setEstimatedAmount(total);
        serviceOrderPersistencePort.save(serviceOrder);
    }

    private List<Long> collectDistinctSupplyIds(List<Service> services) {
        return services.stream()
                .flatMap(s -> s.getNeededSupplies().stream())
                .map(NeededSupply::getIdSupply)
                .distinct()
                .toList();
    }
}
