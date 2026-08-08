package br.com.fiap.postech.adapter.output.vehicle.persistence.entity;

import br.com.fiap.postech.domain.vehicle.model.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicles")
public class VehicleEntity implements Vehicle {
    @Builder.Default
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long id = 0L;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(name = "license_plate",nullable = false, unique = true)
    private String licensePlate;

    @Column(nullable = false)
    private String brand;
    
    @Column(nullable = false)
    private String model;
    
    @Column(name = "vehicle_year", nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private String color;

}
