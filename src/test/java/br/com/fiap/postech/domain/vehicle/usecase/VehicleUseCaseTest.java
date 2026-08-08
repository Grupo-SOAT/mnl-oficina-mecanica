package br.com.fiap.postech.domain.vehicle.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import br.com.fiap.postech.domain.owner.model.Owner;
import br.com.fiap.postech.domain.vehicle.excecption.DuplicatedVehicleException;
import br.com.fiap.postech.domain.vehicle.excecption.InvalidLicensePlateException;
import br.com.fiap.postech.domain.vehicle.excecption.NoMatchingVehiclesException;
import br.com.fiap.postech.domain.vehicle.excecption.VehicleNotFoundException;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import br.com.fiap.postech.port.persistence.owner.OwnerPersistencePort;
import br.com.fiap.postech.port.persistence.vehicle.VehiclePersistencePort;

@ExtendWith(MockitoExtension.class)
public class VehicleUseCaseTest {

    @Mock
    private VehiclePersistencePort persistencePort;

    @Mock
    private OwnerPersistencePort ownerPersistencePort;

    @InjectMocks
    private VehicleUseCase useCase;

    private Vehicle vehicle;


    @BeforeEach
    void setUp() {
        vehicle = mock(Vehicle.class);
    }

    @Test
    void should_delegate_scroll_to_persistence() {
        ScrollPage<Vehicle> expected = ScrollPage.<Vehicle>builder()
                .data(List.of(vehicle))
                .isLast(false)
                .cursor("1")
                .pageSize(10)
                .build();

        when(persistencePort.scroll("ABC1D23", 10, "5"))
                .thenReturn(expected);

        ScrollPage<Vehicle> actual =
                useCase.scroll("ABC1D23", 10, "5");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void should_throw_exception_when_scroll_returns_empty() {
        ScrollPage<Vehicle> emptyPage = ScrollPage.<Vehicle>builder()
                .data(List.of())
                .isLast(true)
                .cursor(null)
                .pageSize(10)
                .build();

        when(persistencePort.scroll("ABC1D23", 10, null))
                .thenReturn(emptyPage);

        assertThatThrownBy(() ->
                useCase.scroll("ABC1D23", 10, null)
        )
                .isInstanceOf(NoMatchingVehiclesException.class)
                .hasMessage("No matching vehicles for license plate: ABC1D23");
    }

    @Test
    void should_return_vehicle_by_id() {
        when(persistencePort.findById(1L))
                .thenReturn(Optional.of(vehicle));

        Vehicle result = useCase.getById(1L);

        assertThat(result).isSameAs(vehicle);

        verify(persistencePort).findById(1L);
    }

    @Test
    void should_throw_exception_when_vehicle_not_found_by_id() {
        when(persistencePort.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.getById(1L)
        )
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found for id: 1");
    }

    @Test
    void should_create_vehicle() {
        Owner owner = mock(Owner.class);

        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("ABC1D23");
        when(vehicle.getYear()).thenReturn(2022);

        when(ownerPersistencePort.findById(10L))
                .thenReturn(Optional.of(owner));

        when(persistencePort.findByLicensePlate("ABC1D23"))
                .thenReturn(Optional.empty());

        when(persistencePort.save(vehicle))
                .thenReturn(vehicle);

        Vehicle result = useCase.create(vehicle);

        assertThat(result).isSameAs(vehicle);

        verify(persistencePort).save(vehicle);
    }

    @Test
    void should_throw_exception_when_license_plate_is_invalid_on_create() {
        when(vehicle.getLicensePlate()).thenReturn("INVALID");

        assertThatThrownBy(() ->
                useCase.create(vehicle)
        )
                .isInstanceOf(InvalidLicensePlateException.class)
                .hasMessage("Invalid license plate: INVALID");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_exception_when_owner_not_found_on_create() {
        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("ABC1D23");
        when(vehicle.getYear()).thenReturn(2022);

        when(ownerPersistencePort.findById(10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.create(vehicle)
        )
                .isInstanceOf(OwnerNotFoundException.class);

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_throw_exception_when_license_plate_already_exists_on_create() {
        Owner owner = mock(Owner.class);

        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("ABC1D23");
        when(vehicle.getYear()).thenReturn(2022);

        when(ownerPersistencePort.findById(10L))
                .thenReturn(Optional.of(owner));

        when(persistencePort.findByLicensePlate("ABC1D23"))
                .thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() ->
                useCase.create(vehicle)
        )
                .isInstanceOf(DuplicatedVehicleException.class)
                .hasMessage("Vehicle already exists for license plate: ABC1D23");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_update_vehicle() {
        Owner owner = mock(Owner.class);

        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("ABC1D23");
        when(vehicle.getId()).thenReturn(1L);
        when(vehicle.getYear()).thenReturn(2022);

        when(ownerPersistencePort.findById(10L))
                .thenReturn(Optional.of(owner));

        when(persistencePort.findByLicensePlate("ABC1D23"))
                .thenReturn(Optional.of(vehicle));

        when(persistencePort.findById(1L))
                .thenReturn(Optional.of(vehicle));

        when(persistencePort.save(vehicle))
                .thenReturn(vehicle);

        Vehicle result = useCase.update(1L, vehicle);

        assertThat(result).isSameAs(vehicle);

        verify(vehicle).setId(1L);
        verify(persistencePort).save(vehicle);
    }

    @Test
    void should_throw_exception_when_updating_with_duplicated_license_plate() {
        Owner owner = mock(Owner.class);
        Vehicle existingVehicle = mock(Vehicle.class);

        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("ABC1D23");
        when(vehicle.getYear()).thenReturn(2022);

        when(ownerPersistencePort.findById(10L))
                .thenReturn(Optional.of(owner));

        when(existingVehicle.getId()).thenReturn(99L);

        when(persistencePort.findByLicensePlate("ABC1D23"))
                .thenReturn(Optional.of(existingVehicle));

        assertThatThrownBy(() ->
                useCase.update(1L, vehicle)
        )
                .isInstanceOf(DuplicatedVehicleException.class)
                .hasMessage("Vehicle already exists for license plate: ABC1D23");
    }

    @Test
    void should_throw_exception_when_vehicle_not_found_on_update() {
        Owner owner = mock(Owner.class);

        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("ABC1D23");
        when(vehicle.getYear()).thenReturn(2022);

        when(ownerPersistencePort.findById(10L))
                .thenReturn(Optional.of(owner));

        when(persistencePort.findByLicensePlate("ABC1D23"))
                .thenReturn(Optional.empty());

        when(persistencePort.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.update(1L, vehicle)
        )
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found for id: 1");

        verify(persistencePort, never()).save(any());
    }

    @Test
    void should_delete_vehicle() {
        when(persistencePort.existsById(1L))
                .thenReturn(true);

        useCase.delete(1L);

        verify(persistencePort).deleteById(1L);
    }

    @Test
    void should_throw_exception_when_deleting_non_existing_vehicle() {
        when(persistencePort.existsById(1L))
                .thenReturn(false);

        assertThatThrownBy(() ->
                useCase.delete(1L)
        )
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessage("Vehicle not found for id: 1");

        verify(persistencePort, never()).deleteById(any());
    }
}
