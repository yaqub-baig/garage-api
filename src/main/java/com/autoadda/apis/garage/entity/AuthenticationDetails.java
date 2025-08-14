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
public class AuthenticationDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private int id;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "encrypted_password")
	private String encryptedPassword;

	@Column(name = "last_logged_tms")
	private Date lastLoggedTms;

	@Column(name = "member_id")
	private int memberId;

	@Column(name = "member_type")
	private String memberType;

	@Column(name = "eff_dte")
	private Date effectiveDate;

	@Column(name = "expiry_date")
	private Date expiryDate;

	@Column(name = "cred_type")
	private String credType;

}
