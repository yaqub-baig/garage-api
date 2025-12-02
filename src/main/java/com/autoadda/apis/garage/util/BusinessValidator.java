package com.autoadda.apis.garage.util;

import com.autoadda.apis.garage.entity.AuthenticationDetails;
import com.autoadda.apis.garage.entity.Customer;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.os.OtherService;
import com.autoadda.apis.garage.exception.*;
import com.autoadda.apis.garage.repository.AuthenticationDetailsRepository;
import com.autoadda.apis.garage.repository.CustomerRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.repository.os.OtherServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessValidator {


    private final GarageRepository garageRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository garageTechnicianRepository;
    private final AuthenticationDetailsRepository authenticationDetailsRepository;
    private final OtherServiceRepository otherServiceRepository;

    /**
     * Validates if the given garage ID exists.
     *
     * @param garageId the ID of the garage to validate
     * @throws GarageNotFoundException if the garage does not exist
     */

    public void validateUsername(String username) throws MemberAlreadyExistException {
        AuthenticationDetails authData = authenticationDetailsRepository.findByUserName ( username );
        if ( null != authData ) {
            throw new MemberAlreadyExistException ( "Username already exist" );
        }
    }

    public void validateTechnician(Integer technicianId) throws MemberNotFoundException {

        if ( ! garageTechnicianRepository.findById ( technicianId ).isPresent ( ) ) {
            throw new MemberNotFoundException ( "technician not exist" );
        }
    }

    public Garage validateAndGetGarage(Integer garageId) throws GarageNotFoundException {
        return garageRepository.findById ( garageId ).orElseThrow ( () -> new GarageNotFoundException ( "Garage not found" ) );
    }

    public Customer validateAndGetCustomer(Integer customerId) throws CustomerNotFoundException {
        return customerRepository.findById ( customerId )
                .orElseThrow ( () -> new CustomerNotFoundException ( "Customer not found" ) );
    }

    public OtherService validateAndGetOtherService(Integer otherServiceId) {
        return otherServiceRepository.findById ( otherServiceId )
                .orElseThrow ( () -> new OtherServiceNotFoundException ( "Other Service not found" ) );
    }
}