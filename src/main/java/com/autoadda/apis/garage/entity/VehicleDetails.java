package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "vin")
    private String vin;

    @Column(name = "year")
    private Integer year;

    @Column(name = "name")
    private String name;

    @Column(name = "odometer")
    private Integer odometer;

    @Column(name = "color")
    private String color;

    @Column(name = "engine_size")
    private String engineSize;

    @Column(name = "no_of_doors")
    private Integer noOfDoors;

    @Column(name = "no_of_seats")
    private Integer noOfSeats;

    @Column(name = "no_of_cylinders")
    private Integer noOfCylinders;

    @Column(name = "no_of_speeds")
    private Integer noOfSpeeds;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "current_mileage")
    private Integer currentMileage;

    @Column(name = "last_service_mileage")
    private Integer lastServiceMileage;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "make")
    private String make;

    @Column(name = "model")
    private String model;

    @Column(name = "status")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "vehicle_owner_id", referencedColumnName = "id")
    private VehicleOwner vehicleOwner;

}
