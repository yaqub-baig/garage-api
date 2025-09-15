package com.autoadda.apis.garage.util;

import com.autoadda.apis.garage.entity.OtherService;
import com.autoadda.apis.garage.exception.*;
import com.autoadda.apis.garage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.autoadda.apis.garage.entity.AuthenticationDetails;
import com.autoadda.apis.garage.entity.Customer;
import com.autoadda.apis.garage.entity.Garage;

@Component
public class BusinessValidator {

	@Autowired
	private GarageRepository garageRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired

	private OtherServiceRepository otherServiceRepository;

	@Autowired
	private TechnicianRepository garageTechnicianRepository;

	@Autowired
	private AuthenticationDetailsRepository authenticationDetailsRepository;

	/**
	 * Validates if the given garage ID exists.
	 *
	 * @param username the ID of the garage to validate
	 * @throws GarageNotFoundException if the garage does not exist
	 */

	public void validateUsername(String username) throws MemberAlreadyExistException {
		AuthenticationDetails authData = authenticationDetailsRepository.findByUserName(username);
		if (null != authData) {
			throw new MemberAlreadyExistException("Username already exist");
		}
	}

	public void validateTechnician(Integer technicianId) throws MemberNotFoundException {
		
		if (!garageTechnicianRepository.findById(technicianId).isPresent()) {
			throw new MemberNotFoundException("technician not exist");
		}
	}

	public Garage validateAndGetGarage(Integer garageId) throws GarageNotFoundException {
		return garageRepository.findById(garageId).orElseThrow(() -> new GarageNotFoundException("Garage not found"));
	}

	public OtherService validateAndGetOtherService(Integer otherServiceId)  throws OtherServiceNotFoundException {
		return  otherServiceRepository.findById(otherServiceId).orElseThrow(() -> new OtherServiceNotFoundException("Other Service Not Found"));
	}

	public Customer validateAndGetCustomer(Integer customerId) throws CustomerNotFoundException {
		return customerRepository.findById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
	}

}