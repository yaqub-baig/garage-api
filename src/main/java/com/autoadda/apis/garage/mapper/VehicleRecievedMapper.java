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
private final MediaFileMapper  mediaFileMapper;
    public VehicleRecievedResponse entityToResponse(VehicleReceived save) {
        VehicleRecievedResponse vehicleRecievedResponse = new VehicleRecievedResponse ( );
        vehicleRecievedResponse.setId ( save.getId ( ) );
        vehicleRecievedResponse.setAppointment ( appointmentDetailMapper.entityToResponse (save.getAppointment ( )) );
        vehicleRecievedResponse.setRecievedBy ( technicianMapper.entityToResponse ( save.getTechnician ( ) ));
        vehicleRecievedResponse.setVehicle (vehicleClient.getVehicleDetailsByIdResponse ( save.getAppointment ().getVehicleId () )  );
        vehicleRecievedResponse.setVehicleRecievedImages ( save.getVehiclePhotos( ));
        vehicleRecievedResponse.setRecievedTime ( save.getCreatedAt () );
        vehicleRecievedResponse.setComment ( save.getComment ( ) );
        return vehicleRecievedResponse;
    }
}
