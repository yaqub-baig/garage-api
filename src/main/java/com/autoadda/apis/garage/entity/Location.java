package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

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
public class Location {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "street_address_1")
	private String streetAddress1;

	@Column(name = "street_address_2")
	private String streetAddress2;

	@Column(name = "city")
	private String city;

	@Column(name = "county")
	private String county;

	@Column(name = "postal_code")
	private String postalCode;

	@Column(name = "state")
	private String state;

	@Column(name = "country")
	private String country;

	@Column(name = "type")
	private String type;

	@Column(name = "insert_tms")
	private LocalDate insertTms;
}