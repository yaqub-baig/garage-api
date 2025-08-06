package com.service.garage.apis.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.service.garage.apis.util.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentDetails {

	private Integer id;

	private LocalDate scheduleDate;

	private LocalTime serviceStartTime;

	private LocalTime serviceEndTime;

	private LocalTime deliveryTime;

	private Status status;

	private String comments;

	private LocalDate insert_tms;

	private Garage garage;

	private Customer customer;

	private VehicleDetails vehicle;

}
