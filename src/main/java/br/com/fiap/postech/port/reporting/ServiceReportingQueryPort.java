package br.com.fiap.postech.port.reporting;

import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;

import java.util.List;
import java.util.Optional;

public interface ServiceReportingQueryPort {

    Optional<ServiceCalculatedAverageTime> findAverageTime(Long catalogServiceId);

    List<ServiceCalculatedAverageTime> findAverageTimes();
}
