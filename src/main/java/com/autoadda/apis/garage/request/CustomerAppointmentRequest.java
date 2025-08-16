package com.autoadda.apis.garage.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
public class CustomerAppointmentRequest {
	private Integer customerId;
	private Integer garageId;
	private Integer vehicleId;
	private List<Integer> serviceIds;
	private LocalDate scheduleDate;
	private LocalTime serviceStartTime;
	private LocalTime serviceEndTime;
	private LocalTime deliveryTime;
	private String comments;

	private  Integer memberId;

			private  String memberType;




}
