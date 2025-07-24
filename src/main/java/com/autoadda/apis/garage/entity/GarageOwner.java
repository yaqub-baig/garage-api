package com.autoadda.apis.garage.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageOwner {

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

	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Garage> ownedGarages = new ArrayList<>();

	@Column(name = "ownership_per")
	private Integer ownershipPer;

	@Column(name = "insert_tms")
	private Date insertTms;
}
