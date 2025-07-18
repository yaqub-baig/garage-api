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
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageSubscription {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "garage_id")
	private Integer garageId;

	@Column(name = "subscription_id")
	private Integer subscriptionId;

	@Column(name = "subscription_type")
	private String subscriptionType;

	@Column(name = "effective_date")
	private Date effectiveDate;

	@Column(name = "expiry_date")
	private Date expiryDate;

	@Column(name = "active_ind")
	private String activeInd;

	@Column(name = "insert_tms")
	private Date insertTms;

}
