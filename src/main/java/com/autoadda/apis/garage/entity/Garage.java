package com.autoadda.apis.garage.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Garage {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

	@Column(name = "legal_name")
	private String legalName;

	@Column(name = "garage_name")
	private String garageName;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "contact_number")
	private String contactNumber;

	@Column(name = "website")
	private String webSite;

	@Column(name = "account_status")
	private Status accountStatus;

	@Column(name = "garage_type")
	private String garageType;

	@Column(name = "chain_id")
	private Integer chainId;

	@Column(name = "created_at")
	private LocalDate createdAt;

	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<GarageService> services = new ArrayList<>();

	@OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Technician> technicians = new ArrayList<>();
	@ManyToOne
	@JoinColumn(name = "garage_owner_id")
	private GarageOwner garageOwner;
}
