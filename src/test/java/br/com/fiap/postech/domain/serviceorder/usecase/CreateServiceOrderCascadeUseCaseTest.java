package br.com.fiap.postech.domain.serviceorder.usecase;

import br.com.fiap.postech.adapter.output.serviceorder.persistence.entity.ServiceOrderEntity;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.domain.service.usecase.ServiceUseCase;
import br.com.fiap.postech.domain.serviceorder.exception.ServiceOrderVehicleDataAbsentException;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrder;
import br.com.fiap.postech.domain.serviceorder.model.ServiceOrderCascadeCreationCommand;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.domain.vehicle.model.VehicleCascadeCreationCommand;
import br.com.fiap.postech.domain.vehicle.usecase.CreateVehicleCascadeUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateServiceOrderCascadeUseCaseTest {

    @Mock
    private ServiceUseCase serviceUseCase;

    @Mock
    private ServiceOrderUseCase serviceOrderUseCase;

    @Mock
    private CreateVehicleCascadeUseCase createVehicleCascadeUseCase;

    @InjectMocks
    private CreateServiceOrderCascadeUseCase useCase;

    @Test
    void should_create_service_order_with_existing_vehicle_id() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(1L)
                .clientId(1L)
                .vehicleId(10L)
                .description("Service order")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(null, serviceOrder, List.of(10L, 20L));

        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        var result = useCase.execute(command);

        assertThat(result).isEqualTo(serviceOrder);
        verify(serviceOrderUseCase).create(serviceOrder);
        verify(serviceUseCase).createFromCatalog(1L, 10L);
        verify(serviceUseCase).createFromCatalog(1L, 20L);
        verify(createVehicleCascadeUseCase, never()).execute(any());
    }

    @Test
    void should_create_vehicle_cascade_and_then_create_service_order() {
        var owner = mock(Owner.class);
        var vehicleCascadeCommand = new VehicleCascadeCreationCommand(owner,
            mock(Vehicle.class));
        var newVehicle = mock(Vehicle.class);
        var serviceOrder = ServiceOrderEntity.builder()
                .id(50L)
                .clientId(2L)
                .vehicleId(null)
                .description("Service order")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(vehicleCascadeCommand, serviceOrder, List.of(10L));

        when(newVehicle.getId()).thenReturn(15L);
        when(createVehicleCascadeUseCase.execute(vehicleCascadeCommand)).thenReturn(newVehicle);
        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        var result = useCase.execute(command);

        assertThat(result).isEqualTo(serviceOrder);
        verify(createVehicleCascadeUseCase).execute(vehicleCascadeCommand);
        verify(serviceOrderUseCase).create(serviceOrder);
        verify(serviceUseCase).createFromCatalog(50L, 10L);
    }

    @Test
    void should_throw_exception_when_both_vehicle_id_and_cascade_command_are_null() {
        var serviceOrder = ServiceOrderEntity.builder()
                .clientId(3L)
                .vehicleId(null)
                .description("Service order")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(null, serviceOrder, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ServiceOrderVehicleDataAbsentException.class);

        verify(serviceOrderUseCase, never()).create(any());
        verify(serviceUseCase, never()).createFromCatalog(anyLong(), anyLong());
        verify(createVehicleCascadeUseCase, never()).execute(any());
    }

    @Test
    void should_create_service_order_with_no_services() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(4L)
                .clientId(4L)
                .vehicleId(20L)
                .description("Service order")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(null, serviceOrder, List.of());

        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        var result = useCase.execute(command);

        assertThat(result).isEqualTo(serviceOrder);
        verify(serviceOrderUseCase).create(serviceOrder);
        verify(serviceUseCase, never()).createFromCatalog(anyLong(), anyLong());
    }

    @Test
    void should_set_vehicle_id_from_created_vehicle_cascade() {
        var owner = mock(Owner.class);
        var vehicleCommand = new VehicleCascadeCreationCommand(owner, mock(Vehicle.class));
        var newVehicle = mock(Vehicle.class);
        var serviceOrder = mock(ServiceOrder.class);
        var command = new ServiceOrderCascadeCreationCommand(vehicleCommand, serviceOrder, Collections.emptyList());

        when(serviceOrder.getVehicleId()).thenReturn(null);
        when(newVehicle.getId()).thenReturn(25L);
        when(createVehicleCascadeUseCase.execute(vehicleCommand)).thenReturn(newVehicle);
        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        useCase.execute(command);

        verify(serviceOrder).setVehicleId(25L);
    }

    @Test
    void should_set_client_id_from_created_vehicle_owner_when_client_is_absent() {
        var owner = mock(Owner.class);
        var vehicleCommand = new VehicleCascadeCreationCommand(owner, mock(Vehicle.class));
        var newVehicle = mock(Vehicle.class);
        var serviceOrder = mock(ServiceOrder.class);
        var command = new ServiceOrderCascadeCreationCommand(vehicleCommand, serviceOrder, Collections.emptyList());

        when(serviceOrder.getVehicleId()).thenReturn(null);
        when(serviceOrder.getClientId()).thenReturn(null);
        when(newVehicle.getId()).thenReturn(25L);
        when(newVehicle.getOwnerId()).thenReturn(99L);
        when(createVehicleCascadeUseCase.execute(vehicleCommand)).thenReturn(newVehicle);
        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        useCase.execute(command);

        verify(serviceOrder).setClientId(99L);
    }

    @Test
    void should_create_all_services_for_service_order() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(100L)
                .clientId(6L)
                .vehicleId(30L)
                .description("Service order")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(null, serviceOrder, List.of(10L, 20L, 30L));

        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        useCase.execute(command);

        verify(serviceUseCase).createFromCatalog(100L, 10L);
        verify(serviceUseCase).createFromCatalog(100L, 20L);
        verify(serviceUseCase).createFromCatalog(100L, 30L);
    }

    @Test
    void should_return_created_service_order() {
        var serviceOrder = ServiceOrderEntity.builder()
                .id(7L)
                .clientId(7L)
                .vehicleId(35L)
                .description("Service order")
                .build();
        var createdServiceOrder = ServiceOrderEntity.builder()
                .id(7L)
                .clientId(7L)
                .vehicleId(35L)
                .description("Service order")
                .status("PENDING")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(null, serviceOrder, List.of());

        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(createdServiceOrder);

        var result = useCase.execute(command);

        assertThat(result).isSameAs(createdServiceOrder);
    }

    @Test
    void should_skip_vehicle_creation_when_service_order_has_vehicle_id() {
        var owner = mock(Owner.class);
        var vehicleCascadeCommand = new VehicleCascadeCreationCommand(owner, mock(Vehicle.class));
        var serviceOrder = ServiceOrderEntity.builder()
                .id(52L)
                .clientId(8L)
                .vehicleId(40L)
                .description("Service order")
                .build();
        var command = new ServiceOrderCascadeCreationCommand(vehicleCascadeCommand, serviceOrder, List.of());

        when(serviceOrderUseCase.create(serviceOrder)).thenReturn(serviceOrder);

        useCase.execute(command);

        verify(createVehicleCascadeUseCase, never()).execute(any());
    }

}
