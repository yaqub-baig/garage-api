package com.autoadda.apis.garage.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The parts of vehicle-api's CompanyResponse garage-api needs. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleCompanyResponse {
    private Long id;
    private String name;
    private UserResponse companyOwner;
}
