package com.service.garage.apis.request;

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

public class GarageServiceRequest {

	private Integer id;
	private String name;
	private String description;
	private Boolean isActive;
	private Double price;
	private Double laborPrice;
	private Integer garageId;
}
