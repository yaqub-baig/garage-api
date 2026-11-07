package com.autoadda.apis.garage.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A driver on a company's roster, as returned by vehicle-api. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDriverResponse {
    private Long id;
    private Long companyId;
    private Integer driverUserId;
    private String driverName;
    private String driverMobileNumber;
}
