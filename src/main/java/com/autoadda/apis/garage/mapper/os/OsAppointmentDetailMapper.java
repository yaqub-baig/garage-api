package com.autoadda.apis.garage.mapper.os;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.os.OsAppointmentDetails;
import com.autoadda.apis.garage.mapper.AvailableServiceMapper;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.response.os.OsAppointmentDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OsAppointmentDetailMapper {
    private final OsSlotMapper osSlotMapper;
    private final OtherServiceMapper otherServiceMapper;
    private final VehicleClient vehicleClient;
    private final AvailableServiceMapper availableServiceMapper;
    private final AvailableServiceRepository availableServiceRepository;

    public OsAppointmentDetailResponse entityToResponse(OsAppointmentDetails osAppointmentDetails) {
        OsAppointmentDetailResponse osAppointmentDetailResponse = new OsAppointmentDetailResponse ( );
        osAppointmentDetailResponse.setId ( osAppointmentDetails.getId ( ) );
        osAppointmentDetailResponse.setAppointmentReferenceId ( osAppointmentDetails.getAppointmentReferenceId ( ) );
        osAppointmentDetailResponse.setScheduleDate ( osAppointmentDetails.getScheduleDate ( ) );
        osAppointmentDetailResponse.setServiceStartTime ( osAppointmentDetails.getServiceStartTime ( ) );
        osAppointmentDetailResponse.setServiceEndTime ( osAppointmentDetails.getServiceEndTime ( ) );
        osAppointmentDetailResponse.setDeliveryTime ( osAppointmentDetails.getDeliveryTime ( ) );
        osAppointmentDetailResponse.setComments ( osAppointmentDetails.getComments ( ) );
        osAppointmentDetailResponse.setStatus ( osAppointmentDetails.getStatus ( ) );
        osAppointmentDetailResponse.setOtherService ( otherServiceMapper.entityToResponse ( osAppointmentDetails.getOtherService ( ) ) );
        osAppointmentDetailResponse.setSlot ( osSlotMapper.toResponse ( osAppointmentDetails.getSlot ( ) ) );
        osAppointmentDetailResponse.setSelectedOsServiceIds ( osAppointmentDetails.getSelectedOtherServiceIds ( ) );
        osAppointmentDetailResponse.setOtherServiceSubscriptionPlanId ( osAppointmentDetails.getOsSubscriptionPlanId ( ) );
        osAppointmentDetailResponse.setVehicleId ( osAppointmentDetails.getVehicleId ( ) );
        osAppointmentDetailResponse.setVehicle ( vehicleClient.getVehicleDetailsByIdResponse ( osAppointmentDetails.getVehicleId ( ) ) );

        return osAppointmentDetailResponse;
    }
}
