package br.com.fiap.postech.domain.reporting.usecase;

import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;

import java.util.List;

public interface ServiceReportingUseCase {
    ServiceCalculatedAverageTime calculateAverageTime(Long catalogServiceId);

    List<ServiceCalculatedAverageTime> calculateAverageTime();
}


