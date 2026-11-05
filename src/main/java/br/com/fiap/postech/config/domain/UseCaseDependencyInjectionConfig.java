package br.com.fiap.postech.config.domain;

import br.com.fiap.postech.domain.authentication.AuthenticationUseCase;
import br.com.fiap.postech.domain.catalogservices.usecase.CatalogServicesUseCase;
import br.com.fiap.postech.domain.owner.usecase.OwnerUseCase;
import br.com.fiap.postech.domain.reporting.usecase.ServiceReportingUseCase;
import br.com.fiap.postech.domain.reporting.usecase.impl.ServiceReportingUseCaseImpl;
import br.com.fiap.postech.domain.supply.usecase.SupplyUseCase;
import br.com.fiap.postech.domain.user.UserUseCase;
import br.com.fiap.postech.domain.vehicle.usecase.VehicleUseCase;
import br.com.fiap.postech.port.authentication.AuthenticationPort;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;
import br.com.fiap.postech.port.reporting.ServiceReportingQueryPort;
import br.com.fiap.postech.port.user.UserPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseDependencyInjectionConfig {

    @Bean
    public ServiceReportingUseCase catalogServiceReportingUseCase(ServiceReportingQueryPort queryPort) {
        return new ServiceReportingUseCaseImpl(queryPort);
    }

    @Bean
    public SupplyUseCase supplyUseCase(SupplyPersistencePort persistencePort) {
        return new SupplyUseCase(persistencePort);
    }

    @Bean
    public CatalogServicesUseCase catalogServicesUseCase(
            CatalogServicesPersistencePort persistencePort,
            SupplyPersistencePort supplyPersistencePort
    ) {
        return new CatalogServicesUseCase(persistencePort, supplyPersistencePort);
    }

    @Bean
    public UserUseCase userUseCase(UserPort userPort) {
        return new UserUseCase(userPort);
    }

    @Bean
    public VehicleUseCase vehicleUseCase(
            VehiclePersistencePort persistencePort,
            OwnerPersistencePort ownerPersistencePort
    ) {
        return new VehicleUseCase(persistencePort, ownerPersistencePort);
    }

    @Bean
    public OwnerUseCase ownerUseCase(OwnerPersistencePort persistencePort) {
        return new OwnerUseCase(persistencePort);
    }

    @Bean
    public AuthenticationUseCase authenticationUseCase(
            AuthenticationPort authenticationPort,
            UserPort userPort
    ) {
        return new AuthenticationUseCase(authenticationPort, userPort);
    }
}
