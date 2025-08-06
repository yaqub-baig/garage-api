package com.service.garage.apis.entity;

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
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GaragePendingActions {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "license_update_ind")
	private String licenseUpdateInd;
	
	@Column(name = "payment_ind")
	private String paymentInd;
	
	@Column(name = "tech_update_ind")
	private String techUpdateInd;
	
	@Column(name = "owner_update_ind")
	private String ownerUpdateInd;
	
	@Column(name = "email_verify_ind")
	private String emailVerifyInd;
	
	@Column(name = "service_updte_ind")
	private String serviceUpdateInd;
	
	@Column(name = "search_ind")
	private String searchInd;
	
	@Column(name = "subscription_id")
	private Integer subscriptionId;
	
	@Column(name = "insert_tms")
	private LocalDate insertTms;
	
}
