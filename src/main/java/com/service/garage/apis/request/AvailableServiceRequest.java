package com.service.garage.apis.request;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableServiceRequest {

	private Integer id;

	private String name;

	private String description;

	private Double price;

	private Double laborPrice;

    private List<GarageServiceRequest> garageLinks = new ArrayList<>();

}
