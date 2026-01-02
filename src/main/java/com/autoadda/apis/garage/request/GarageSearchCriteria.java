package com.autoadda.apis.garage.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageSearchCriteria {
    private String query;                      // Universal search parameter
    private String brand;                      // Vehicle brand filter
    private String city;                       // Location city filter
    private String zip;                        // Postal code filter
    private String garageName;                 // Garage name filter
    private List<Integer> availableServiceIds; // Service IDs filter
    private String model;                      // Vehicle model filter
}
