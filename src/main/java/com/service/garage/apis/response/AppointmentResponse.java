package com.service.garage.apis.response;

import java.util.List;

import com.service.garage.apis.entity.AppointmentDetails;
import com.service.garage.apis.entity.AppointmentServices;
import com.service.garage.apis.entity.JobCard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppointmentResponse {
	private AppointmentDetails appointment;
	private List<AppointmentServices> services;
	private List<JobCard> jobcards;

}
