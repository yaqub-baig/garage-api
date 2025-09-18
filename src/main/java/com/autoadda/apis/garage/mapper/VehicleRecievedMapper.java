package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.VehicleReceived;
import com.autoadda.apis.garage.response.VehicleRecievedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleRecievedMapper {
    private final AppointmentDetailMapper appointmentDetailMapper;
    private final VehicleClient vehicleClient;
    private final TechnicianMapper technicianMapper;

    public VehicleRecievedResponse entityToResponse(VehicleReceived save) {
        VehicleRecievedResponse vehicleRecievedResponse = new VehicleRecievedResponse ( );
        vehicleRecievedResponse.setId ( save.getId ( ) );
        vehicleRecievedResponse.setAppointment ( appointmentDetailMapper.entityToResponse ( save.getAppointment ( ) ) );
        vehicleRecievedResponse.setRecievedBy ( technicianMapper.entityToResponse ( save.getTechnician ( ) ) );
        vehicleRecievedResponse.setVehicle ( vehicleClient.getVehicleDetailsByIdResponse ( save.getAppointment ( ).getVehicleId ( ) ) );
        vehicleRecievedResponse.setVehicleRecievedImages ( save.getVehiclePhotos ( ) );
        vehicleRecievedResponse.setRecievedTime ( save.getCreatedAt ( ) );
        vehicleRecievedResponse.setComment ( save.getComment ( ) );
        vehicleRecievedResponse.setIsMainKeyReceived ( save.getIsMainKeyReceived ( ) != null ? save.getIsMainKeyReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsSpareKeyReceived ( save.getIsSpareKeyReceived ( ) != null ? save.getIsSpareKeyReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsToolKitReceived ( save.getIsToolKitReceived ( ) != null ? save.getIsToolKitReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsJackReceived ( save.getIsJackReceived ( ) != null ? save.getIsJackReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsWheelSpannerReceived ( save.getIsWheelSpannerReceived ( ) != null ? save.getIsWheelSpannerReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsSpareTyreReceived ( save.getIsSpareTyreReceived ( ) != null ? save.getIsSpareTyreReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsFireExtinguisherReceived ( save.getIsFireExtinguisherReceived ( ) != null ? save.getIsFireExtinguisherReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsFirstAidKitReceived ( save.getIsFirstAidKitReceived ( ) != null ? save.getIsFirstAidKitReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsCentralLockRemoteReceived ( save.getIsCentralLockRemoteReceived ( ) != null ? save.getIsCentralLockRemoteReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsRCBookReceived ( save.getIsRCBookReceived ( ) != null ? save.getIsRCBookReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsInsuranceCopyReceived ( save.getIsInsuranceCopyReceived ( ) != null ? save.getIsInsuranceCopyReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsRCBookReceived ( save.getIsPUCReceived ( ) != null ? save.getIsPUCReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setIsPUCReceived ( save.getIsPUCReceived ( ) != null ? save.getIsPUCReceived ( ) : Boolean.FALSE );
        vehicleRecievedResponse.setStatus ( save.getStatus ( ) );
        return vehicleRecievedResponse;
    }
}
