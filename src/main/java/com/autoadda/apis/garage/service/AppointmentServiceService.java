package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.AppointmentServicesMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.AppointmentServiceRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.AppointmentServiceRequest;
import com.autoadda.apis.garage.response.AppointmentServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentServiceService {
    private final AppointmentServiceRepository appointmentServiceRepo;
    private final AppointmentServicesMapper appointmentServicesMapper;
    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final TechnicianRepository technicianRepository;

    public AppointmentServiceResponse createAppointmentService(AppointmentServiceRequest request) {
        AppointmentServices appointmentService = appointmentServicesMapper.requestToEntity ( request );
        AppointmentDetails appointmentDetails = appointmentDetailsRepository.findById ( request.getAppointmentId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "appointment Id not found" ) );
        appointmentService.setAppointmentId ( appointmentDetails.getId ( ) );
        if ( request.getTechnicianId ( ) != null ) {
            Technician technician = technicianRepository.findById ( request.getTechnicianId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "technician Id not found" ) );
            appointmentService.setTechnician ( technician );
        }
        appointmentService = appointmentServiceRepo.save ( appointmentService );
        return appointmentServicesMapper.entityToResponse ( appointmentService );
    }


    public AppointmentServiceResponse updateAppointmentService(Integer id, AppointmentServiceRequest request) {
        AppointmentServices appointmentService = appointmentServiceRepo.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "AppointmentService not found" ) );

        if ( request.getAppointmentId ( ) != null ) {
            AppointmentDetails appointmentDetails = appointmentDetailsRepository.findById ( request.getAppointmentId ( ) )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "appointment Id not found" ) );
            appointmentService.setAppointmentId ( appointmentDetails.getId ( ) );
        }

        if ( request.getTechnicianId ( ) != null ) {
            Technician technician = technicianRepository.findById ( request.getTechnicianId ( ) )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "technician Id not found" ) );
            appointmentService.setTechnician ( technician );
        }

        if ( request.getStatus ( ) != null ) {
            appointmentService.setStatus ( request.getStatus ( ) );
        }

        appointmentService = appointmentServiceRepo.save ( appointmentService );
        return appointmentServicesMapper.entityToResponse ( appointmentService );
    }

}
