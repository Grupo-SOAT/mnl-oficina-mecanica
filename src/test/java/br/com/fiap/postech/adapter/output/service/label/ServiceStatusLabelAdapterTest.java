package br.com.fiap.postech.adapter.output.service.label;

import org.junit.jupiter.api.BeforeEach;
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
class ServiceStatusLabelAdapterTest {

    @Mock
    private MessageSource messageSource;

    private ServiceStatusLabelAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ServiceStatusLabelAdapter(messageSource);
    }

    @Test
    void shouldResolveKnownServiceStatus() {
        when(messageSource.getMessage(eq("service.status.APPROVED"), any(), any(Locale.class)))
                .thenReturn("Aprovado");

        String label = adapter.resolve("APPROVED");

        assertThat(label).isEqualTo("Aprovado");
    }

    @Test
    void shouldReturnNullWhenStatusIsNull() {
        String label = adapter.resolve(null);
        assertThat(label).isNull();
    }
}
