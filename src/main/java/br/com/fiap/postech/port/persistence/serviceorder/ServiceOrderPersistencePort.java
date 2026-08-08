package br.com.fiap.postech.port.persistence.serviceorder;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;

import java.util.Optional;

public interface ServiceOrderPersistencePort {

    ScrollPage<ServiceOrder> scroll(String status, Long clientId, Long vehicleId, Integer pageSize, String cursor);

    Optional<ServiceOrder> findById(Long id);

    ServiceOrder save(ServiceOrder serviceOrder);

    void deleteById(Long id);

    boolean existsById(Long id);
}
