package br.com.fiap.postech.domain.vehicle.model;

public interface Vehicle {
    Long getId();

    void setId(Long id);

    Long getOwnerId();

    void setOwnerId(Long ownerId);

    String getLicensePlate();

    void setLicensePlate(String licensePlate);
    
    String getBrand();

    void setBrand(String brand);

    String getModel();

    void setModel(String model);

    Integer getYear();

    void setYear(Integer year);

    String getColor();

    void setColor(String color);
}
