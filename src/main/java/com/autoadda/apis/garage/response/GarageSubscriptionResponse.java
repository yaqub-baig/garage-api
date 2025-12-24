package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageSubscriptionResponse {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private Integer durationMonths;
    private String benefits;
    private String brands;
    private Map<Integer, Integer> serviceQuantity;
    private List<AvailableServiceResponse> services;
    private List<GarageServiceResponse> garageServices;
    private Integer garageId;
    private Location garageLocation;
    private String garageName;
    private Boolean isActive;
}