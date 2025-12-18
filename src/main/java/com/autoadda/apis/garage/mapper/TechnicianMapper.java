package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.response.TechnicianResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class TechnicianMapper {
    private final GarageMapper garageMapper;
    private final AuthClient authClient;
    private final AvailableServiceMapper availableServiceMapper;

    public TechnicianResponse entityToResponse(Technician technician) {
        TechnicianResponse technicianResponse = new TechnicianResponse ( );
        technicianResponse.setId ( technician.getId ( ) );
        technicianResponse.setFirstName ( technician.getFirstName ( ) );
        technicianResponse.setEmiratesId ( technician.getEmiratesId ( ) );
        technicianResponse.setVatNumber ( technician.getVatNumber ( ) );
        technicianResponse.setEmailId ( technician.getEmailId ( ) );
        technicianResponse.setMobileNumber ( technician.getMobileNumber ( ) );
        technicianResponse.setSkillSet ( technician.getSkillSet ( ).stream ( ).map ( availableServiceMapper::toResponse ).toList ( ) );
        technicianResponse.setStatus ( technician.getStatus ( ) );
        technicianResponse.setEmployeeRole ( technician.getEmployeeRole ( ) );

        // Map many-to-many garages
        technicianResponse.setGarages (
            technician.getGarages() != null
                ? technician.getGarages().stream()
                    .map(garageMapper::entityToResponse)
                    .toList()
                : Collections.emptyList()
        );

        // Map new fields
        technicianResponse.setBrand ( technician.getBrand ( ) );
        technicianResponse.setModel ( technician.getModel ( ) );
        technicianResponse.setDescription ( technician.getDescription ( ) );
        technicianResponse.setYearOfExperience ( technician.getYearOfExperience ( ) );
        technicianResponse.setCity ( technician.getCity ( ) );
        technicianResponse.setIsAvailableForService ( technician.getIsAvailableForService ( ) );
        technicianResponse.setAutoAddaUser ( authClient.getUser ( technician.getAutoAddaId ( ) ) );
        return technicianResponse;
    }

    public Technician updateTechnician(Technician technician, TechnicianRequest technicianRequest) {
        if ( technicianRequest.getEmailId ( ) != null )
            technician.setEmailId ( technicianRequest.getEmailId ( ) );
        if ( technicianRequest.getFirstName ( ) != null )
            technician.setFirstName ( technicianRequest.getFirstName ( ) );
        if ( technicianRequest.getEmiratesId ( ) != null )
            technician.setEmiratesId ( technicianRequest.getEmiratesId ( ) );
        if ( technicianRequest.getVatNumber ( ) != null )
            technician.setVatNumber ( technicianRequest.getVatNumber ( ) );
        if ( technicianRequest.getMobileNumber ( ) != null )
            technician.setMobileNumber ( technicianRequest.getMobileNumber ( ) );
        if ( technicianRequest.getStatus ( ) != null )
            technician.setStatus ( technicianRequest.getStatus ( ) );
        if ( technicianRequest.getEmployeeRole ( ) != null )
            technician.setEmployeeRole ( technicianRequest.getEmployeeRole ( ) );

        // Update new fields
        if ( technicianRequest.getBrand ( ) != null )
            technician.setBrand ( technicianRequest.getBrand ( ) );
        if ( technicianRequest.getModel ( ) != null )
            technician.setModel ( technicianRequest.getModel ( ) );
        if ( technicianRequest.getDescription ( ) != null )
            technician.setDescription ( technicianRequest.getDescription ( ) );
        if ( technicianRequest.getYearOfExperience ( ) != null )
            technician.setYearOfExperience ( technicianRequest.getYearOfExperience ( ) );
        if ( technicianRequest.getCity ( ) != null )
            technician.setCity ( technicianRequest.getCity ( ) );
        if ( technicianRequest.getIsAvailableForService ( ) != null )
            technician.setIsAvailableForService ( technicianRequest.getIsAvailableForService ( ) );

        return technician;
    }
}