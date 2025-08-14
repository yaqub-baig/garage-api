package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AppointmentInvoiceBreakDown {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
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
	
	@Column(name = "appointment_id")
	private Integer appointmentId;
	
	@Column(name = "garage_id")
	private Integer garageId;
	
	@Column(name = "service")
	private String service;
	
	@Column(name = "invoice_id")
	private Integer invoiceId;
	
}
