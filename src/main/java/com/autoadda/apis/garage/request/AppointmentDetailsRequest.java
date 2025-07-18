package com.autoadda.apis.garage.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import com.autoadda.apis.garage.util.Status;

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

public class AppointmentDetailsRequest {

	private LocalDate scheduleDate;
	private LocalTime serviceStartTime;
	private Date deliveryTime;
	private Status status;
	private String comments;
	private LocalDate insert_tms;
	private Integer garageId;

}
