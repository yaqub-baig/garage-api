package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.response.TechnicianResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        technicianResponse.setGarage ( garageMapper.entityToResponse ( technician.getGarage ( ) ) );
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
        return technician;
    }
}