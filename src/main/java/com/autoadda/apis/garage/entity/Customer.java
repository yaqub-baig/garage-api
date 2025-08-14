package com.autoadda.apis.garage.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "customer_type")
	private String customerType;

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

	@Column(name = "website_txt")
	private String webSite;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "vat_number")
	private String vatNumber;

	@Column(name = "account_status")
	private String accountStatus;

	@Column(name = "insert_tms")
	private Date insertTms;

}
