package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.response.AppointmentDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentDetailMapper {
    private final SlotMapper appointmentSlotMapper;
    private final GarageMapper garageMapper;
    private final VehicleClient vehicleClient;
    private final AvailableServiceMapper availableServiceMapper;
    private final AvailableServiceRepository availableServiceRepository;

    public AppointmentDetailResponse entityToResponse(AppointmentDetails appointmentDetail) {
        AppointmentDetailResponse appointmentDetailResponse = new AppointmentDetailResponse ( );
        appointmentDetailResponse.setId ( appointmentDetail.getId ( ) );
        appointmentDetailResponse.setAppointmentReferenceId ( appointmentDetail.getAppointmentReferenceId ( ) );
        appointmentDetailResponse.setScheduleDate ( appointmentDetail.getScheduleDate ( ) );
        appointmentDetailResponse.setServiceStartTime ( appointmentDetail.getServiceStartTime ( ) );
        appointmentDetailResponse.setServiceEndTime ( appointmentDetail.getServiceEndTime ( ) );
        appointmentDetailResponse.setDeliveryTime ( appointmentDetail.getDeliveryTime ( ) );
        appointmentDetailResponse.setComments ( appointmentDetail.getComments ( ) );
        appointmentDetailResponse.setStatus ( appointmentDetail.getStatus ( ) );
        appointmentDetailResponse.setGarage ( garageMapper.entityToResponse ( appointmentDetail.getGarage ( ) ) );
        appointmentDetailResponse.setSlot ( appointmentSlotMapper.toResponse ( appointmentDetail.getSlot ( ) ) );
        appointmentDetailResponse.setSelectedGarageServiceIds ( appointmentDetail.getSelectedGarageServiceIds ( ) );
        appointmentDetailResponse.setGarageSubscriptionPlanId ( appointmentDetail.getGarageSubscriptionPlanId ( ) );
        appointmentDetailResponse.setVehicleId ( appointmentDetail.getVehicleId ( ) );
        appointmentDetailResponse.setVehicle ( vehicleClient.getVehicleDetailsByIdResponse ( appointmentDetail.getVehicleId ( ) ) );
//        appointmentDetailResponse.setGarageServicesResponse (
//                Arrays.stream ( appointmentDetail.getSelectedGarageServiceIds ( ).split ( "," ) ).mapToInt ( Integer::getInteger ).toArray ();
//
//
//
//        );
        return appointmentDetailResponse;
    }
}
