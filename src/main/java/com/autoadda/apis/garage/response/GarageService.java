package com.autoadda.apis.garage.response;

import java.util.Date;

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
public class GarageService {
	private Integer id;

	private String service;

	private String description;

	private Integer categoryId;

	private Date insertTms;

}
