package com.service.garage.apis.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AppointmentInvoice {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "comments")
	private String comments;
	
	@Column(name = "insert_tms")
	private LocalDate insert_tms;
	
	@Column(name = "net_amount")
	private Double netAmount;
	
	@Column(name = "tax_amount")
	private Double taxAmount;
	
	@Column(name = "total_amount")
	private Double totalAmount;
	
	@Column(name = "tax_id")
	private String taxId;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "garage_id", referencedColumnName = "id")
    private Garage garage;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "appointment_id", referencedColumnName = "id")
    private AppointmentDetails appointment;
	
}
