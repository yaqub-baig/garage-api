package com.autoadda.apis.garage.request;

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
public class AppointmentInvoiceRequest {
	private Integer appointmentId;
	private Integer garageId;
	private String status;
	private List<ServiceInvoice> service;
	private Double taxAmount;
	private String comments;
	private String taxId;
}
