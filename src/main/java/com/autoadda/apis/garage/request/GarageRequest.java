package com.autoadda.apis.garage.request;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class GarageRequest {

	private Integer id;

	private String legalName;

	private String garageName;

	private String emailId;

	private String contactNumber;

	private String webSite;

	private Status accountStatus;

	private String garageType;

	private Integer chainId;
	
	private LocalDate createdAt;

}
