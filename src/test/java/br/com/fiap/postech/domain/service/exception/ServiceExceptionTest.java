package br.com.fiap.postech.domain.service.exception;

import br.com.fiap.postech.domain.service.exception.reason.ServiceExceptionReason;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceExceptionTest {

    @Test
    void service_not_found_should_carry_correct_message_and_reason() {
        ServiceNotFoundException exception = new ServiceNotFoundException(42L);

        assertThat(exception.getMessage()).isEqualTo("Service not found for id: 42");
        assertThat(exception.reason).isEqualTo(ServiceExceptionReason.SERVICE_NOT_FOUND);
    }

    @Test
    void no_matching_services_should_carry_correct_message() {
        NoMatchingServicesException exception = new NoMatchingServicesException(10L);

        assertThat(exception.getMessage()).isEqualTo("No matching services for service order id: 10");
    }
}
