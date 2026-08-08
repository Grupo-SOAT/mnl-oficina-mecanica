package br.com.fiap.postech.domain.vehicle.usecase;

import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.domain.owner.usecase.OwnerUseCase;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleOwnerDataAbsentException;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.domain.vehicle.model.VehicleCascadeCreationCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateVehicleCascadeUseCaseTest {

    @Mock
    private OwnerUseCase ownerUseCase;

    @Mock
    private VehicleUseCase vehicleUseCase;

    @InjectMocks
    private CreateVehicleCascadeUseCase useCase;

    @Test
    void should_create_vehicle_with_existing_owner_id() {
        Vehicle vehicle = mock(Vehicle.class);
        Owner owner = mock(Owner.class);
        VehicleCascadeCreationCommand command = mock(VehicleCascadeCreationCommand.class);

        when(command.vehicle()).thenReturn(vehicle);
        when(command.owner()).thenReturn(owner);
        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicleUseCase.create(vehicle)).thenReturn(vehicle);

        Vehicle result = useCase.execute(command);

        assertThat(result).isSameAs(vehicle);
        verify(vehicleUseCase).create(vehicle);
        verify(ownerUseCase, never()).create(any());
    }

    @Test
    void should_create_owner_and_then_create_vehicle_when_owner_id_is_null() {
        Vehicle vehicle = mock(Vehicle.class);
        Owner owner = mock(Owner.class);
        Owner createdOwner = mock(Owner.class);
        VehicleCascadeCreationCommand command = mock(VehicleCascadeCreationCommand.class);

        when(command.vehicle()).thenReturn(vehicle);
        when(command.owner()).thenReturn(owner);
        when(vehicle.getOwnerId()).thenReturn(null);
        when(ownerUseCase.create(owner)).thenReturn(createdOwner);
        when(createdOwner.getId()).thenReturn(5L);
        when(vehicleUseCase.create(vehicle)).thenReturn(vehicle);

        Vehicle result = useCase.execute(command);

        assertThat(result).isSameAs(vehicle);
        verify(ownerUseCase).create(owner);
        verify(vehicle).setOwnerId(5L);
        verify(vehicleUseCase).create(vehicle);
    }

    @Test
    void should_throw_exception_when_both_owner_id_and_owner_are_null() {
        Vehicle vehicle = mock(Vehicle.class);
        VehicleCascadeCreationCommand command = mock(VehicleCascadeCreationCommand.class);

        when(command.vehicle()).thenReturn(vehicle);
        when(command.owner()).thenReturn(null);
        when(vehicle.getOwnerId()).thenReturn(null);

        assertThatThrownBy(() ->
                useCase.execute(command)
        )
                .isInstanceOf(VehicleOwnerDataAbsentException.class);

        verify(vehicleUseCase, never()).create(any());
        verify(ownerUseCase, never()).create(any());
    }

    @Test
    void should_not_create_owner_when_vehicle_already_has_owner_id() {
        Vehicle vehicle = mock(Vehicle.class);
        Owner owner = mock(Owner.class);
        VehicleCascadeCreationCommand command = mock(VehicleCascadeCreationCommand.class);

        when(command.vehicle()).thenReturn(vehicle);
        when(command.owner()).thenReturn(owner);
        when(vehicle.getOwnerId()).thenReturn(20L);
        when(vehicleUseCase.create(vehicle)).thenReturn(vehicle);

        useCase.execute(command);

        verify(ownerUseCase, never()).create(any());
        verify(vehicleUseCase).create(vehicle);
    }

    @Test
    void should_set_owner_id_on_vehicle_before_creating_it() {
        Vehicle vehicle = mock(Vehicle.class);
        Owner owner = mock(Owner.class);
        Owner createdOwner = mock(Owner.class);
        VehicleCascadeCreationCommand command = mock(VehicleCascadeCreationCommand.class);

        when(command.vehicle()).thenReturn(vehicle);
        when(command.owner()).thenReturn(owner);
        when(vehicle.getOwnerId()).thenReturn(null);
        when(ownerUseCase.create(owner)).thenReturn(createdOwner);
        when(createdOwner.getId()).thenReturn(15L);
        when(vehicleUseCase.create(vehicle)).thenReturn(vehicle);

        useCase.execute(command);

        verify(vehicle).setOwnerId(15L);
    }

    @Test
    void should_use_created_owner_id_for_vehicle_creation() {
        Vehicle vehicle = mock(Vehicle.class);
        Owner owner = mock(Owner.class);
        Owner createdOwner = mock(Owner.class);
        VehicleCascadeCreationCommand command = mock(VehicleCascadeCreationCommand.class);

        when(command.vehicle()).thenReturn(vehicle);
        when(command.owner()).thenReturn(owner);
        when(vehicle.getOwnerId()).thenReturn(null);
        when(ownerUseCase.create(owner)).thenReturn(createdOwner);
        when(createdOwner.getId()).thenReturn(25L);
        when(vehicleUseCase.create(vehicle)).thenReturn(vehicle);

        useCase.execute(command);

        verify(vehicle).setOwnerId(25L);
        verify(vehicleUseCase).create(vehicle);
    }

}
