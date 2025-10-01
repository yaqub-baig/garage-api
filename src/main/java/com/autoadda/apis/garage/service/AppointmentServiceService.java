package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.AppointmentServicesMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.AppointmentServiceRepository;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.AppointmentServiceRequest;
import com.autoadda.apis.garage.response.AppointmentServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceService {
    private final AppointmentServiceRepository appointmentServiceRepo;
    private final AppointmentServicesMapper appointmentServicesMapper;
    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final TechnicianRepository technicianRepository;
    private final GarageServiceRepository garageServiceRepository;

    public AppointmentServiceResponse createAppointmentService(AppointmentServiceRequest request) {
        AppointmentServices appointmentService = appointmentServicesMapper.requestToEntity ( request );
        AppointmentDetails appointmentDetails = appointmentDetailsRepository.findById ( request.getAppointmentId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "appointment Id not found" ) );
        appointmentService.setAppointmentId ( appointmentDetails.getId ( ) );
        if ( request.getTechnicianId ( ) != null ) {
            Technician technician = technicianRepository.findById ( request.getTechnicianId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "technician Id not found" ) );
            appointmentService.setTechnician ( technician );
        }

        appointmentService.setService ( garageServiceRepository.findById (
                request.getServiceId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "Service not found" )
        ) );
        appointmentService.setPrice ( appointmentService.getService ( ).getPrice ( ) );
        appointmentService.setLabourPrice ( appointmentService.getService ( ).getLabourPrice ( ) );
        appointmentService = appointmentServiceRepo.save ( appointmentService );

        appointmentDetails.setSelectedGarageServiceIds ( appointmentDetails.getSelectedGarageServiceIds ( ) + "," + appointmentService.getService ( ).getId ( ) );
//
        appointmentDetailsRepository.save ( appointmentDetails );
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
        if ( request.getLabourPrice ( ) != null ) {
            appointmentService.setLabourPrice ( request.getLabourPrice ( ) );
        }
        if ( request.getPrice ( ) != null ) {
            appointmentService.setPrice ( request.getPrice ( ) );
        }
        appointmentService = appointmentServiceRepo.save ( appointmentService );
        return appointmentServicesMapper.entityToResponse ( appointmentService );
    }


    public List<AppointmentServiceResponse> getAppointmentServiceByAppointmentId(Integer appointmentId) {
        List<AppointmentServices> appointmentService = appointmentServiceRepo.findByAppointmentId ( appointmentId );
        return appointmentService.stream ( ).map ( appointmentServicesMapper::entityToResponse ).toList ( );
    }

    public void deleteAppointmentService(Integer id) {
        AppointmentServices appointmentService = appointmentServiceRepo.findById ( id )
                .orElseThrow ( () -> new ResourceNotFoundException ( "AppointmentService not found" ) );
        appointmentServiceRepo.delete ( appointmentService );
    }
}
