package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobCardAdditionalRequest {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name = "status")
	private Status status;
	
	@Column(name = "technician_id")
    private Integer technicianId;
	
	@Column(name = "customer_id")
    private Integer customerId;
	
	@Column(name = "garage_id")
    private Integer garageId;
    
	@Column(name = "request_type")
    private String requestType;
	
	@Column(name = "comments")
	private String comments;
	
	@Column(name = "insert_tms")
	private LocalDate insert_tms;
	
}
