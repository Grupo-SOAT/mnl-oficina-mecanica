package br.com.fiap.postech.adapter.output.serviceorder.label;

import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ServiceOrderStatusLabelAdapter implements ServiceOrderStatusLabelPort {

    private final MessageSource messageSource;

    public ServiceOrderStatusLabelAdapter(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String resolve(String status) {
        if (status == null) return null;
        return messageSource.getMessage("serviceorder.status." + status, null, Locale.getDefault());
    }
}
