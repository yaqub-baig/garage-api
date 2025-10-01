package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FutureAppointment {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer vehicleId;
    private String garageId;
    private String serviceType;

    private LocalDate scheduledDate;
    private Integer estimatedMileage;

    @Enumerated(EnumType.STRING)
    private Status status; // SCHEDULED, CONFIRMED, CANCELLED

    private String createdBy; // SYSTEM or USER
}