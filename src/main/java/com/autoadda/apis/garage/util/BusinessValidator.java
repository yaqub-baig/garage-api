package com.autoadda.apis.garage.util;


import com.autoadda.apis.garage.entity.Customer;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.exception.*;
import com.autoadda.apis.garage.repository.CustomerRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessValidator {


    private final GarageRepository garageRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository garageTechnicianRepository;


    /**
     * Validates if the given garage ID exists.
     *
     * @param garageId the ID of the garage to validate
     * @throws GarageNotFoundException if the garage does not exist
     */



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

}