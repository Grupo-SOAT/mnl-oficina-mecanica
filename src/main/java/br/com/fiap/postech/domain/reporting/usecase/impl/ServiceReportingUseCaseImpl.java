package br.com.fiap.postech.domain.reporting.usecase.impl;

import br.com.fiap.postech.domain.reporting.exception.ReportingNoMatchingServiceException;
import br.com.fiap.postech.domain.reporting.exception.ReportingServiceNotFoundException;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.domain.reporting.usecase.ServiceReportingUseCase;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;

import java.util.List;

public class ServiceReportingUseCaseImpl implements ServiceReportingUseCase {
    private final ServicePersistencePort persistencePort;

    public ServiceReportingUseCaseImpl(ServicePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public ServiceCalculatedAverageTime calculateAverageTime(Long catalogServiceId) {
        final var result = persistencePort.calculateAverageTime(catalogServiceId);

        if (result == null) throw new ReportingServiceNotFoundException(catalogServiceId);

        return result;
    }

    @Override
    public List<ServiceCalculatedAverageTime> calculateAverageTime() {
        final var result = persistencePort.calculateAverageTime();

        if (result == null || result.isEmpty()) throw new ReportingNoMatchingServiceException();

        return result;
    }
}
