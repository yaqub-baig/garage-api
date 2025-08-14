package com.autoadda.apis.garage.request;

import java.time.LocalDate;
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
	List<Integer> serviceIds;
	private LocalDate scheduleDate;
	private Integer memberId;
	private String memberType;
	
}
