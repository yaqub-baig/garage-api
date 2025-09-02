package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request object for creating or updating a GarageSubscription.
 * Contains all necessary fields from GarageSubscription but uses garageId instead of Garage object.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageSubscriptionRequest {

    private String name;
    private String description;
    private Double price;
    private Integer durationMonths;
    private List<Integer> garageIds;
    private List<Integer> servicesIds;
    private Map<Integer, Integer> services;
    private String benefits;
    private String brands;

}
