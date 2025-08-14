package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("INDIVIDUAL")
@Getter
@Setter
@NoArgsConstructor
public class IndividualOwner extends VehicleOwner {

    private String firstName;
    private String lastName;

    private LocalDate dateOfBirth;
}
