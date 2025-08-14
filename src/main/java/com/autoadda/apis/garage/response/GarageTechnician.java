package com.autoadda.apis.garage.response;

import java.util.Date;

import com.autoadda.apis.garage.util.Status;

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

public class GarageTechnician {

	private Integer id;

	private String emiratesId;

	private String firstName;

	private String lastName;

	private String emailId;

	private String mobileNumber;

	private String vatNumber;

	private Status accountStatus;

	private String profileImg;

	private Integer garageId;

	private Date insertTms;
}
