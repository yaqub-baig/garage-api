package com.autoadda.apis.garage.entity;

import java.time.LocalDate;

import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
public class AppointmentServices {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column(name = "status")
	private Status status;
	
	@Column(name = "appointment_id")
	private Integer appointmentId;
	
	@Column(name = "insert_tms")
	private LocalDate insert_tms;
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "service_id", referencedColumnName = "id")
    private GarageService service;
	
	
	@OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "technician_id", referencedColumnName = "id")
    private GarageTechnician technician;
	
}
