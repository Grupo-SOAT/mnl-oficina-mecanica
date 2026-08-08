package br.com.fiap.postech.domain.reporting.usecase.impl;

import br.com.fiap.postech.domain.reporting.exception.ReportingNoMatchingServiceException;
import br.com.fiap.postech.domain.reporting.exception.ReportingServiceNotFoundException;
import br.com.fiap.postech.domain.reporting.model.ServiceCalculatedAverageTime;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ServiceReportingUseCaseImplTest {

    @Mock
    private ServicePersistencePort persistencePort;

    @InjectMocks
    private ServiceReportingUseCaseImpl useCase;

    @Test
    void should_return_average_time_for_catalog_service_when_found() {
        ServiceCalculatedAverageTime expected = ServiceCalculatedAverageTime.builder()
                .id(10L)
                .name("Troca de oleo")
                .totalCreated(5L)
                .totalCompleted(4L)
                .averageTimeBetweenCreateAndComplete(6.0)
                .averageTimeBetweenStartAndComplete(4.5)
                .averageTimeBetweenApproveAndComplete(3.0)
                .averageTimeAwaitingBudgetApproval(1.5)
                .build();
        when(persistencePort.calculateAverageTime(10L)).thenReturn(expected);

        ServiceCalculatedAverageTime result = useCase.calculateAverageTime(10L);

        assertThat(result).isSameAs(expected);
        verify(persistencePort).calculateAverageTime(10L);
    }

    @Test
    void should_throw_not_found_when_average_time_for_catalog_service_is_null() {
        when(persistencePort.calculateAverageTime(42L)).thenReturn(null);

        assertThatThrownBy(() -> useCase.calculateAverageTime(42L))
                .isInstanceOf(ReportingServiceNotFoundException.class)
                .hasMessage("Could not find any service for catalog service id: 42");
    }

    @Test
    void should_return_average_time_for_all_services_when_result_has_items() {
        List<ServiceCalculatedAverageTime> expected = List.of(
                ServiceCalculatedAverageTime.builder().id(1L).name("Freio").totalCreated(2L).build(),
                ServiceCalculatedAverageTime.builder().id(2L).name("Suspensao").totalCreated(3L).build()
        );
        when(persistencePort.calculateAverageTime()).thenReturn(expected);

        List<ServiceCalculatedAverageTime> result = useCase.calculateAverageTime();

        assertThat(result).isSameAs(expected);
        verify(persistencePort).calculateAverageTime();
    }

    @Test
    void should_throw_no_matching_when_average_time_for_all_services_is_null() {
        when(persistencePort.calculateAverageTime()).thenReturn(null);

        assertThatThrownBy(() -> useCase.calculateAverageTime())
                .isInstanceOf(ReportingNoMatchingServiceException.class)
                .hasMessage("Could not find any service to generate report");
    }

    @Test
    void should_throw_no_matching_when_average_time_for_all_services_is_empty() {
        when(persistencePort.calculateAverageTime()).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.calculateAverageTime())
                .isInstanceOf(ReportingNoMatchingServiceException.class)
                .hasMessage("Could not find any service to generate report");
    }
}

