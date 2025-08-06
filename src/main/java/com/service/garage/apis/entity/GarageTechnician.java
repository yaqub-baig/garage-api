package com.service.garage.apis.entity;

import java.util.Date;

import com.service.garage.apis.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageTechnician {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "emirates_id")
	private String emiratesId;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "vat_number")
	private String vatNumber;

	@Column(name = "account_status")
	private Status accountStatus;
	
	@Column(name = "profile_img")
	private String profileImg;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "insert_tms")
	private Date insertTms;
}
