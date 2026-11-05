package br.com.fiap.postech.domain.reporting.usecase.impl;

import br.com.fiap.postech.domain.reporting.exception.ReportingNoMatchingServiceException;
import br.com.fiap.postech.domain.reporting.exception.ReportingServiceNotFoundException;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.domain.reporting.usecase.ServiceReportingUseCase;
import br.com.fiap.postech.port.reporting.ServiceReportingQueryPort;

import java.util.List;

public class ServiceReportingUseCaseImpl implements ServiceReportingUseCase {
    private final ServiceReportingQueryPort queryPort;

    public ServiceReportingUseCaseImpl(ServiceReportingQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public ServiceCalculatedAverageTime calculateAverageTime(Long catalogServiceId) {
        return queryPort.findAverageTime(catalogServiceId)
                .orElseThrow(() -> new ReportingServiceNotFoundException(catalogServiceId));
    }

    @Override
    public List<ServiceCalculatedAverageTime> calculateAverageTime() {
        final var result = queryPort.findAverageTimes();

        if (result == null || result.isEmpty()) throw new ReportingNoMatchingServiceException();

        return result;
    }
}
