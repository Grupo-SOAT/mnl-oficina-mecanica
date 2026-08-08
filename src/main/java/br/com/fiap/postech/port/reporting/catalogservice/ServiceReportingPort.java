package br.com.fiap.postech.port.reporting.catalogservice;

import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;

import java.util.List;

public interface ServiceReportingPort {
    byte[] writePDF(ServiceCalculatedAverageTime input);

    String writeCSV(List<ServiceCalculatedAverageTime> items);
}


