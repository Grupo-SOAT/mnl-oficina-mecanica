package br.com.fiap.postech.adapter.output.service.label;

import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ServiceStatusLabelAdapter implements ServiceStatusLabelPort {

    private final MessageSource messageSource;

    public ServiceStatusLabelAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String resolve(String status) {
        if (status == null) return null;
        return messageSource.getMessage("service.status." + status, null, Locale.getDefault());
    }
}
