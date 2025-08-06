package com.service.garage.apis.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceInvoice {
	private String service;
	private Double amount;
	private String comments;
}
