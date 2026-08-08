package br.com.fiap.postech.config.domain;

import br.com.fiap.postech.domain.authentication.AuthenticationUseCase;
import br.com.fiap.postech.domain.catalogservices.usecase.CatalogServicesUseCase;
import br.com.fiap.postech.domain.owner.usecase.OwnerUseCase;
import br.com.fiap.postech.domain.reporting.usecase.ServiceReportingUseCase;
import br.com.fiap.postech.domain.reporting.usecase.impl.ServiceReportingUseCaseImpl;
import br.com.fiap.postech.domain.service.usecase.ChangeServiceStatusUseCase;
import br.com.fiap.postech.domain.service.usecase.ServiceUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.ChangeServiceOrderStatusUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.CreateServiceOrderCascadeUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.EstimateServiceOrderAmountUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.FinalizeInspectionUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.ProcessBudgetDecisionUseCase;
import br.com.fiap.postech.domain.serviceorder.usecase.ServiceOrderUseCase;
import br.com.fiap.postech.domain.supply.usecase.SupplyUseCase;
import br.com.fiap.postech.domain.user.UserUseCase;
import br.com.fiap.postech.domain.vehicle.usecase.CreateVehicleCascadeUseCase;
import br.com.fiap.postech.domain.vehicle.usecase.VehicleUseCase;
import br.com.fiap.postech.port.authentication.AuthenticationPort;
import br.com.fiap.postech.port.persistence.catalogService.CatalogServicesPersistencePort;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import br.com.fiap.postech.port.persistence.service.ServicePersistencePort;
import br.com.fiap.postech.port.persistence.service.ServiceStatusLabelPort;
import br.com.fiap.postech.port.persistence.serviceorder.BudgetApprovalTokenPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderPersistencePort;
import br.com.fiap.postech.port.persistence.serviceorder.ServiceOrderStatusLabelPort;
import br.com.fiap.postech.port.persistence.supply.SupplyPersistencePort;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;
import br.com.fiap.postech.port.message.serviceorder.BudgetApprovalRequestPublisherPort;
import br.com.fiap.postech.port.user.UserPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseDependencyInjectionConfig {

    @Value("${app.budget.token.ttl-hours:48}")
    private int tokenTtlHours;

    @Bean
    public ServiceReportingUseCase catalogServiceReportingUseCase(ServicePersistencePort persistencePort) {
        return new ServiceReportingUseCaseImpl(persistencePort);
    }

    @Bean
    public ServiceUseCase serviceUseCase(
            ServicePersistencePort persistencePort,
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            CatalogServicesPersistencePort catalogServicesPersistencePort,
            SupplyPersistencePort supplyPersistencePort,
            ServiceStatusLabelPort statusLabelPort
    ) {
        return new ServiceUseCase(
                persistencePort,
                serviceOrderPersistencePort,
                catalogServicesPersistencePort,
                supplyPersistencePort,
                statusLabelPort
        );
    }

    @Bean
    public ChangeServiceStatusUseCase changeServiceStatusUseCase(
            ServicePersistencePort servicePersistencePort,
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            SupplyPersistencePort supplyPersistencePort,
            ServiceStatusLabelPort statusLabelPort
    ) {
        return new ChangeServiceStatusUseCase(
                servicePersistencePort,
                serviceOrderPersistencePort,
                supplyPersistencePort,
                statusLabelPort
        );
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
    public ServiceOrderUseCase serviceOrderUseCase(
            ServiceOrderPersistencePort persistencePort,
            OwnerPersistencePort ownerPersistencePort,
            VehiclePersistencePort vehiclePersistencePort,
            ServiceOrderStatusLabelPort statusLabelPort
    ) {
        return new ServiceOrderUseCase(persistencePort, ownerPersistencePort, vehiclePersistencePort, statusLabelPort);
    }

    @Bean
    public EstimateServiceOrderAmountUseCase estimateServiceOrderAmountUseCase(
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            ServicePersistencePort servicePersistencePort,
            SupplyPersistencePort supplyPersistencePort
    ) {
        return new EstimateServiceOrderAmountUseCase(
                serviceOrderPersistencePort, servicePersistencePort, supplyPersistencePort);
    }

    @Bean
    public ChangeServiceOrderStatusUseCase changeServiceOrderStatusUseCase(
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            ServicePersistencePort servicePersistencePort,
            ChangeServiceStatusUseCase changeServiceStatusUseCase,
            FinalizeInspectionUseCase finalizeInspectionUseCase,
            EstimateServiceOrderAmountUseCase estimateServiceOrderAmountUseCase,
            ServiceOrderStatusLabelPort statusLabelPort
    ) {
        return new ChangeServiceOrderStatusUseCase(
                serviceOrderPersistencePort,
                servicePersistencePort,
                changeServiceStatusUseCase,
                finalizeInspectionUseCase,
                estimateServiceOrderAmountUseCase,
                statusLabelPort
        );
    }

    @Bean
    public CreateServiceOrderCascadeUseCase createServiceOrderCascadeUseCase(
            ServiceUseCase serviceUseCase,
            ServiceOrderUseCase serviceOrderUseCase,
            CreateVehicleCascadeUseCase createVehicleCascadeUseCase
    ) {
        return new CreateServiceOrderCascadeUseCase(
                serviceUseCase,
                serviceOrderUseCase,
                createVehicleCascadeUseCase
        );
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
    public CreateVehicleCascadeUseCase createVehicleCascadeUseCase(
            OwnerUseCase ownerUseCase,
            VehicleUseCase vehicleUseCase
    ) {
        return new CreateVehicleCascadeUseCase(ownerUseCase, vehicleUseCase);
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

    @Bean
    public FinalizeInspectionUseCase finalizeInspectionUseCase(
            ServiceOrderPersistencePort serviceOrderPersistencePort,
            BudgetApprovalTokenPersistencePort budgetApprovalTokenPersistencePort,
            BudgetApprovalRequestPublisherPort budgetApprovalRequestPublisherPort
    ) {
        return new FinalizeInspectionUseCase(
                serviceOrderPersistencePort,
                budgetApprovalTokenPersistencePort,
                budgetApprovalRequestPublisherPort,
                tokenTtlHours
        );
    }

    @Bean
    public ProcessBudgetDecisionUseCase processBudgetDecisionUseCase(
            ChangeServiceOrderStatusUseCase changeServiceOrderStatusUseCase,
            BudgetApprovalTokenPersistencePort budgetApprovalTokenPersistencePort
    ) {
        return new ProcessBudgetDecisionUseCase(changeServiceOrderStatusUseCase, budgetApprovalTokenPersistencePort);
    }
}
