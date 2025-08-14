package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location information used in requests.
 * Matches the structure of the Location entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {
    private String streetAddress1;
    private String streetAddress2;
    private String city;
    private String postalCode;
    private String area;
    private String state;
    private String country;
    private String gmap;
}
