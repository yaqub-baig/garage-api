package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.VehicleReceived;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.VehicleRecievedMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.repository.VehicleReceivedRepository;
import com.autoadda.apis.garage.request.VehicleReceivedRequest;
import com.autoadda.apis.garage.response.VehicleRecievedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleReceivedService {
    private final VehicleReceivedRepository vehicleReceivedRepository;
    private final AppointmentDetailsRepository appointmentRepository;
    private final TechnicianRepository technicianRepository;
    private final MediaFileRepository mediaFileRepository;
    private final VehicleRecievedMapper vehicleRecievedMapper;

    public VehicleRecievedResponse receiveVehicle(VehicleReceivedRequest request) {
        VehicleReceived vehicleReceived = new VehicleReceived ( );
        vehicleReceived.setAppointment (appointmentRepository.findById ( request.getAppointmentId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "appointment not found" ) ) );
        if(vehicleReceived.getComment ()!=null) {
            vehicleReceived.setComment ( vehicleReceived.getComment ( ) );
        }

        if (request.getVehicleImages ()!=null && !request.getVehicleImages ( ).isEmpty ( )) {
            vehicleReceived.setVehiclePhotos ( mediaFileRepository.findAllById ( request.getVehicleImages ( ) ) );
        }
        vehicleReceived.setTechnician ( technicianRepository.findById ( request.getReceiverId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "technician not found" ) ) );
        VehicleReceived save = vehicleReceivedRepository.save ( vehicleReceived );
        return vehicleRecievedMapper.entityToResponse ( save );
    }

    public VehicleRecievedResponse getVehicleReceivedDetails(Integer vehicleRecievedId) {
        VehicleReceived vehicleReceived = vehicleReceivedRepository.findById ( vehicleRecievedId ).orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle received details not found for this id: " + vehicleRecievedId ) );
        return vehicleRecievedMapper.entityToResponse ( vehicleReceived );
    }

    public List<VehicleRecievedResponse> getAllRecievedCarByGarageId(Integer garageId) {
        List<VehicleReceived> byGarageId = vehicleReceivedRepository.findByAppointmentGarageId ( garageId );
        return byGarageId.stream ( ).map ( vehicleRecievedMapper::entityToResponse ).toList ( );
    }
}
