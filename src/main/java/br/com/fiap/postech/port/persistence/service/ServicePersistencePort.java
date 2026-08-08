package br.com.fiap.postech.port.persistence.service;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.domain.service.model.Service;

import java.util.List;
import java.util.Optional;

public interface ServicePersistencePort {
    ScrollPage<Service> scroll(Long serviceOrderId, Long serviceId, String status, Integer pageSize, String cursor);
    Optional<Service> findByIdAndServiceOrderId(Long id, Long serviceOrderId);
    List<Service> findAllByServiceOrderId(Long serviceOrderId);
    Service save(Service service);
    void deleteById(Long id);
    ServiceCalculatedAverageTime calculateAverageTime(Long id);
    List<ServiceCalculatedAverageTime> calculateAverageTime();
}


