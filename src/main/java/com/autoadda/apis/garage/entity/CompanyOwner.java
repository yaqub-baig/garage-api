package com.autoadda.apis.garage.entity;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyOwner extends VehicleOwner {

    private String companyName;
    private String tradeLicenseNumber;

    private String contactPerson;
}
