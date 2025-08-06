package com.service.garage.apis.request;

import com.service.garage.apis.util.Status;

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
public class AppointmentStatusRequest {
	private Integer appointmentId;
	private Integer memberId;
	private String memberType;
	private Status status;
	private String comments;
}
