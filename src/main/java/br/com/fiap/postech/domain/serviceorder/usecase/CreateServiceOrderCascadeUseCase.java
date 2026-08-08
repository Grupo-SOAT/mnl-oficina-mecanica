package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.domain.service.usecase.ServiceUseCase;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderVehicleDataAbsentException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderCascadeCreationCommand;
import br.com.fiap.postech.domain.vehicle.usecase.CreateVehicleCascadeUseCase;

public class CreateServiceOrderCascadeUseCase {

    private final ServiceUseCase serviceUseCase;
    private final ServiceOrderUseCase serviceOrderUseCase;
    private final CreateVehicleCascadeUseCase createVehicleCascadeUseCase;

    public CreateServiceOrderCascadeUseCase(
            ServiceUseCase serviceUseCase,
            ServiceOrderUseCase serviceOrderUseCase,
            CreateVehicleCascadeUseCase createVehicleCascadeUseCase
    ) {
        this.serviceUseCase = serviceUseCase;
        this.serviceOrderUseCase = serviceOrderUseCase;
        this.createVehicleCascadeUseCase = createVehicleCascadeUseCase;
    }

    public ServiceOrder execute(ServiceOrderCascadeCreationCommand command) {
        var serviceOrder = command.serviceOrder();
        final var vehicleCascadeCreationCommand = command.vehicleCascadeCreationCommand();

        if (serviceOrder.getVehicleId() == null && vehicleCascadeCreationCommand == null)
            throw new ServiceOrderVehicleDataAbsentException();

        if (serviceOrder.getVehicleId() == null && vehicleCascadeCreationCommand != null) {
            final var newVehicle = createVehicleCascadeUseCase.execute(vehicleCascadeCreationCommand);

            if (newVehicle == null) throw new ServiceOrderVehicleDataAbsentException();

            serviceOrder.setVehicleId(newVehicle.getId());
            if (serviceOrder.getClientId() == null) {
                serviceOrder.setClientId(newVehicle.getOwnerId());
            }
        }

        serviceOrder = serviceOrderUseCase.create(serviceOrder);

        for (var catalogServiceId : command.catalogServiceIds()) {
            serviceUseCase.createFromCatalog(serviceOrder.getId(), catalogServiceId);
        }

        return serviceOrder;
    }

}
