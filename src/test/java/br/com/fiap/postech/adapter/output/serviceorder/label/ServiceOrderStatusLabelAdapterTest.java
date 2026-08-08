package br.com.fiap.postech.adapter.output.serviceorder.label;

import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderStatusLabelAdapterTest {

    @Mock
    private MessageSource messageSource;

    @Test
    void shouldResolvePendingToRecebida() {
        when(messageSource.getMessage(eq("serviceorder.status.PENDING"), any(), any(Locale.class)))
                .thenReturn("Recebida");

        ServiceOrderStatusLabelPort adapter = new ServiceOrderStatusLabelAdapter(messageSource);

        String label = adapter.resolve("PENDING");

        assertThat(label).isEqualTo("Recebida");
    }
}
