package br.com.fiap.postech.adapter.output.vehicle.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.vehicle.persistence.entity.VehicleEntity;
import br.com.fiap.postech.adapter.output.vehicle.persistence.repository.VehicleRepository;
import br.com.fiap.postech.domain.vehicle.model.Vehicle;

@ExtendWith(MockitoExtension.class)
public class VehiclePersistenceAdapterTest {


    @Mock
    private VehicleRepository repository;

    @InjectMocks
    private VehiclePersistenceAdapter adapter;

    @Test
    void should_find_vehicle_by_id() {
        VehicleEntity entity = VehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC1234")
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(entity));

        Optional<Vehicle> result = adapter.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getLicensePlate()).isEqualTo("ABC1234");

        verify(repository).findById(1L);
    }

    @Test
    void should_return_empty_when_vehicle_not_found_by_id() {
        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<Vehicle> result = adapter.findById(1L);

        assertThat(result).isEmpty();

        verify(repository).findById(1L);
    }

    @Test
    void should_find_vehicle_by_license_plate() {
        VehicleEntity entity = VehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC1234")
                .build();

        when(repository.findByLicensePlate("ABC1234"))
                .thenReturn(Optional.of(entity));

        Optional<Vehicle> result =
                adapter.findByLicensePlate("ABC1234");

        assertThat(result).isPresent();
        assertThat(result.get().getLicensePlate())
                .isEqualTo("ABC1234");

        verify(repository).findByLicensePlate("ABC1234");
    }

    @Test
    void should_save_vehicle_entity_directly() {
        VehicleEntity entity = VehicleEntity.builder()
                .id(1L)
                .ownerId(10L)
                .licensePlate("ABC1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .color("Preto")
                .build();

        when(repository.save(entity)).thenReturn(entity);

        Vehicle result = adapter.save(entity);

        assertThat(result).isSameAs(entity);

        verify(repository).save(entity);
    }

    @Test
    void should_convert_and_save_vehicle_interface() {
        Vehicle vehicle = mock(Vehicle.class);

        when(vehicle.getId()).thenReturn(1L);
        when(vehicle.getOwnerId()).thenReturn(10L);
        when(vehicle.getLicensePlate()).thenReturn("XYZ9999");
        when(vehicle.getBrand()).thenReturn("Honda");
        when(vehicle.getModel()).thenReturn("Civic");
        when(vehicle.getYear()).thenReturn(2023);
        when(vehicle.getColor()).thenReturn("Branco");

        when(repository.save(any(VehicleEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Vehicle result = adapter.save(vehicle);

        assertThat(result).isInstanceOf(VehicleEntity.class);

        VehicleEntity savedEntity = (VehicleEntity) result;

        assertThat(savedEntity.getOwnerId()).isEqualTo(10L);
        assertThat(savedEntity.getLicensePlate()).isEqualTo("XYZ9999");
        assertThat(savedEntity.getBrand()).isEqualTo("Honda");
        assertThat(savedEntity.getModel()).isEqualTo("Civic");
        assertThat(savedEntity.getYear()).isEqualTo(2023);
        assertThat(savedEntity.getColor()).isEqualTo("Branco");

        verify(repository).save(any(VehicleEntity.class));
    }

    @Test
    void should_delete_vehicle_by_id() {
        adapter.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void should_check_if_vehicle_exists_by_id() {
        when(repository.existsById(1L)).thenReturn(true);

        boolean result = adapter.existsById(1L);

        assertThat(result).isTrue();

        verify(repository).existsById(1L);
    }

    @Test
    void should_scroll_all_vehicles_when_license_plate_is_null() {
        VehicleEntity entity = VehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC1234")
                .build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Vehicle> result =
                adapter.scroll(null, 10, null);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getLicensePlate())
                .isEqualTo("ABC1234");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void should_scroll_vehicles_by_license_plate() {
        VehicleEntity entity = VehicleEntity.builder()
                .id(1L)
                .licensePlate("XYZ9999")
                .build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Vehicle> result =
                adapter.scroll("XYZ9999", 10, null);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getLicensePlate())
                .isEqualTo("XYZ9999");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }
    
    @Test
    void should_scroll_all_vehicles_when_license_plate_is_blank() {
        VehicleEntity entity = VehicleEntity.builder()
                .id(1L)
                .licensePlate("ABC1234")
                .build();

        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        ScrollPage<Vehicle> result =
                adapter.scroll("   ", 10, null);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).getLicensePlate())
                .isEqualTo("ABC1234");

        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }
}
