package com.service.garage.apis.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request object for creating or updating a GarageSubscription.
 * Contains all necessary fields from GarageSubscription but uses garageId instead of Garage object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageSubscriptionRequest {

    private List<Integer> garageIds;
    private String name;
    private String description;
    private Double price;
    private Integer durationMonths;
    private List<Integer> servicesIds;
    private String benefits;
    private String brands;

}
