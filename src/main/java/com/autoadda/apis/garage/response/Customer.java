package com.autoadda.apis.garage.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

	private Integer id;

	private String customerType;

	private String emiratesId;

	private String firstName;

	private String lastName;

	private String emailId;

	private String mobileNumber;

	private String webSite;

	private String companyName;

	private String vatNumber;

	private String accountStatus;

	private Date insertTms;

}
