package com.autoadda.apis.garage.response;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Garage {

	private Integer id;

	private String legalName;

	private String garageName;

	private String emailId;

	private String contactNumber;

	private String webSite;

	private Status accountStatus;

	private String garageType;

	private Integer chainId;

	private LocalDate insertTms;

}
