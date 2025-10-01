package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.AppointmentServiceRequest;
import com.autoadda.apis.garage.response.AppointmentServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentServicesMapper {
    private final GarageServiceMapper garageServiceMapper;
    private final GarageServiceRepository garageServiceRepository;
    private final TechnicianMapper technicianMapper;
    private final TechnicianRepository technicianRepository;

    public AppointmentServiceResponse entityToResponse(AppointmentServices service) {
        AppointmentServiceResponse appointmentServiceResponse = new AppointmentServiceResponse ( );
        appointmentServiceResponse.setId ( service.getId ( ) );
        appointmentServiceResponse.setAppointmentId ( service.getAppointmentId ( ) );
        appointmentServiceResponse.setStatus ( service.getStatus ( ) );
        appointmentServiceResponse.setTechnician ( service.getTechnician ( ) != null ? technicianMapper.entityToResponse ( technicianRepository.findById ( service.getTechnician ( ).getId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Id not found" ) ) ) : null );
        appointmentServiceResponse.setService ( service.getService ( ) != null ? garageServiceMapper.entityToResponse ( garageServiceRepository.findById ( service.getService ( ).getId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "id not found" ) ) ) : null );
        appointmentServiceResponse.setPrice ( service.getPrice ( ) );
        appointmentServiceResponse.setLabourPrice ( service.getLabourPrice ( ) );
        return appointmentServiceResponse;
    }

    public AppointmentServices requestToEntity(AppointmentServiceRequest request) {

        return AppointmentServices.builder ( )
                .status ( request.getStatus ( ) )
                .build ( );
    }

}
