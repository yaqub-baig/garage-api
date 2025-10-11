package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.VehicleReceived;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.VehicleRecievedMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.repository.VehicleReceivedRepository;
import com.autoadda.apis.garage.request.VehicleReceivedRequest;
import com.autoadda.apis.garage.response.VehicleRecievedResponse;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VehicleReceivedService {
    private final VehicleReceivedRepository vehicleReceivedRepository;
    private final AppointmentDetailsRepository appointmentRepository;
    private final TechnicianRepository technicianRepository;
    private final MediaFileRepository mediaFileRepository;
    private final VehicleRecievedMapper vehicleRecievedMapper;

    public Boolean getBooleanValue(Boolean value) {
        return value != null ? value : Boolean.FALSE;
    }

    @Transactional
    public VehicleRecievedResponse receiveVehicle(VehicleReceivedRequest request) {
        VehicleReceived vehicleReceived = new VehicleReceived ( );
        AppointmentDetails appointment = appointmentRepository.findById ( request.getAppointmentId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "appointment not found" ) );
        appointment.setStatus ( Status.VEHICLE_RECEIVED );
        appointmentRepository.save ( appointment );
        vehicleReceived.setAppointment ( appointment );

        if ( request.getComment ( ) != null ) {
            vehicleReceived.setComment ( request.getComment ( ) );
        }
        log.info ( "  requested images ------->" + request.getVehicleImages ( ).stream ( ).toList ( ) );
        if ( request.getVehicleImages ( ) != null && ! request.getVehicleImages ( ).isEmpty ( ) ) {
            List<MediaFile> mediaFiles = mediaFileRepository.findAllById ( request.getVehicleImages ( ) );
            if ( mediaFiles.size ( ) != request.getVehicleImages ( ).size ( ) ) {
                throw new ResourceNotFoundException ( "Some media files not found" );
            }
            vehicleReceived.setVehiclePhotos ( mediaFiles );
            log.info ( "  requested images ------->" + vehicleReceived.getVehiclePhotos ( ).stream ( ).map ( MediaFile::getId ).toList ( ) );
        }

        vehicleReceived.setTechnician ( technicianRepository.findById ( request.getReceiverId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "technician not found" ) ) );

        vehicleReceived.setIsMainKeyReceived ( getBooleanValue ( request.getIsMainKeyReceived ( ) ) );
        vehicleReceived.setIsSpareKeyReceived ( getBooleanValue ( request.getIsSpareKeyReceived ( ) ) );
        vehicleReceived.setIsToolKitReceived ( getBooleanValue ( request.getIsToolKitReceived ( ) ) );
        vehicleReceived.setIsJackReceived ( getBooleanValue ( request.getIsJackReceived ( ) ) );
        vehicleReceived.setIsWheelSpannerReceived ( getBooleanValue ( request.getIsWheelSpannerReceived ( ) ) );
        vehicleReceived.setIsSpareTyreReceived ( getBooleanValue ( request.getIsSpareTyreReceived ( ) ) );
        vehicleReceived.setIsFireExtinguisherReceived ( getBooleanValue ( request.getIsFireExtinguisherReceived ( ) ) );
        vehicleReceived.setIsFirstAidKitReceived ( getBooleanValue ( request.getIsFirstAidKitReceived ( ) ) );
        vehicleReceived.setIsCentralLockRemoteReceived ( getBooleanValue ( request.getIsCentralLockRemoteReceived ( ) ) );
        vehicleReceived.setIsRCBookReceived ( getBooleanValue ( request.getIsRCBookReceived ( ) ) );
        vehicleReceived.setIsInsuranceCopyReceived ( getBooleanValue ( request.getIsInsuranceCopyReceived ( ) ) );
        vehicleReceived.setIsPUCReceived ( getBooleanValue ( request.getIsPUCReceived ( ) ) );

        VehicleReceived save = vehicleReceivedRepository.save ( vehicleReceived );
//        log.info ( "  requested images ------->" + save.getVehiclePhotos ( ).stream ( ).map ( MediaFile::getId ).toList ( ) );

        return vehicleRecievedMapper.entityToResponse ( save );
    }

    @Transactional(readOnly = true)
    public VehicleRecievedResponse getVehicleReceivedDetails(Integer vehicleRecievedId) {
        VehicleReceived vehicleReceived = vehicleReceivedRepository.findById ( vehicleRecievedId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle received details not found for this id: " + vehicleRecievedId ) );
        return vehicleRecievedMapper.entityToResponse ( vehicleReceived );
    }

    @Transactional(readOnly = true)
    public List<VehicleRecievedResponse> getAllRecievedCarByGarageId(Integer garageId) {
        List<VehicleReceived> byGarageId = vehicleReceivedRepository.findByAppointmentGarageId ( garageId );
        return byGarageId.stream ( ).map ( vehicleRecievedMapper::entityToResponse ).toList ( );
    }

    @Transactional
    public VehicleRecievedResponse updateVehicleReceived(Integer vehicleRecievedId, VehicleReceivedRequest request) {
        VehicleReceived vehicleReceived = vehicleReceivedRepository.findById ( vehicleRecievedId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Vehicle received details not found for this id: " + vehicleRecievedId ) );

        // Typically in update operations, you wouldn't change the appointment association
        // unless there's a specific business requirement to do so
        // vehicleReceived.setAppointment(appointmentRepository.findById(request.getAppointmentId())
        //         .orElseThrow(() -> new ResourceNotFoundException("appointment not found")));

        if ( request.getComment ( ) != null ) {
            vehicleReceived.setComment ( request.getComment ( ) );
        }

        if ( request.getVehicleImages ( ) != null ) {
            if ( request.getVehicleImages ( ).isEmpty ( ) ) {
                // Clear existing photos
                vehicleReceived.setVehiclePhotos ( new ArrayList<> ( ) );
            } else {
                // Update with new photos
                vehicleReceived.setVehiclePhotos ( mediaFileRepository.findAllById ( request.getVehicleImages ( ) ) );
            }
        }

        vehicleReceived.setTechnician ( technicianRepository.findById ( request.getReceiverId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "technician not found" ) ) );

        // Use the helper method consistently
        vehicleReceived.setIsMainKeyReceived ( getBooleanValue ( request.getIsMainKeyReceived ( ) ) );
        vehicleReceived.setIsSpareKeyReceived ( getBooleanValue ( request.getIsSpareKeyReceived ( ) ) );
        vehicleReceived.setIsToolKitReceived ( getBooleanValue ( request.getIsToolKitReceived ( ) ) );
        vehicleReceived.setIsJackReceived ( getBooleanValue ( request.getIsJackReceived ( ) ) );
        vehicleReceived.setIsWheelSpannerReceived ( getBooleanValue ( request.getIsWheelSpannerReceived ( ) ) );
        vehicleReceived.setIsSpareTyreReceived ( getBooleanValue ( request.getIsSpareTyreReceived ( ) ) );
        vehicleReceived.setIsFireExtinguisherReceived ( getBooleanValue ( request.getIsFireExtinguisherReceived ( ) ) );
        vehicleReceived.setIsFirstAidKitReceived ( getBooleanValue ( request.getIsFirstAidKitReceived ( ) ) );
        vehicleReceived.setIsCentralLockRemoteReceived ( getBooleanValue ( request.getIsCentralLockRemoteReceived ( ) ) );
        vehicleReceived.setIsRCBookReceived ( getBooleanValue ( request.getIsRCBookReceived ( ) ) );
        vehicleReceived.setIsInsuranceCopyReceived ( getBooleanValue ( request.getIsInsuranceCopyReceived ( ) ) );
        vehicleReceived.setIsPUCReceived ( getBooleanValue ( request.getIsPUCReceived ( ) ) );

        VehicleReceived updatedVehicle = vehicleReceivedRepository.save ( vehicleReceived );
        return vehicleRecievedMapper.entityToResponse ( updatedVehicle );
    }
}