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
public class GaragePaymentDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "card_number")
	private String cardNumber;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "exp_dte")
	private String expDate;

	@Column(name = "insert_tms")
	private Date insertTms;
}
