package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.response.AppointmentDetailResponse;
import com.autoadda.apis.garage.response.GarageServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentDetailMapper {
    private final SlotMapper appointmentSlotMapper;
    private final GarageMapper garageMapper;
    private final VehicleClient vehicleClient;
    private final AvailableServiceMapper availableServiceMapper;
    private final AvailableServiceRepository availableServiceRepository;
    private final GarageServiceRepository garageServiceRepository;
    private final GarageServiceMapper garageServiceMapper;

    public AppointmentDetailResponse entityToResponse(AppointmentDetails appointmentDetail) {
        AppointmentDetailResponse appointmentDetailResponse = new AppointmentDetailResponse ( );
        log.info ( "-------------->16" );

        appointmentDetailResponse.setId ( appointmentDetail.getId ( ) );
        appointmentDetailResponse.setAppointmentReferenceId ( appointmentDetail.getAppointmentReferenceId ( ) );
        appointmentDetailResponse.setScheduleDate ( appointmentDetail.getScheduleDate ( ) );
        appointmentDetailResponse.setServiceStartTime ( appointmentDetail.getServiceStartTime ( ) );
        appointmentDetailResponse.setServiceEndTime ( appointmentDetail.getServiceEndTime ( ) );
        appointmentDetailResponse.setDeliveryTime ( appointmentDetail.getDeliveryTime ( ) );
        appointmentDetailResponse.setComments ( appointmentDetail.getComments ( ) );
        appointmentDetailResponse.setStatus ( appointmentDetail.getStatus ( ) );
        log.info ( "-------------->17" );

        appointmentDetailResponse.setGarage ( garageMapper.entityToResponse ( appointmentDetail.getGarage ( ) ) );
        log.info ( "-------------->18" );

        appointmentDetailResponse.setSlot ( appointmentSlotMapper.toResponse ( appointmentDetail.getSlot ( ) ) );
        appointmentDetailResponse.setSelectedGarageServiceIds ( appointmentDetail.getSelectedGarageServiceIds ( ) );

        appointmentDetailResponse.setGarageSubscriptionPlanId ( appointmentDetail.getGarageSubscriptionPlanId ( ) );
        appointmentDetailResponse.setVehicleId ( appointmentDetail.getVehicleId ( ) );
        log.info ( "-------------->19" );

        appointmentDetailResponse.setVehicle ( vehicleClient.getVehicleDetailsByIdResponse ( appointmentDetail.getVehicleId ( ) ) );
        List<GarageServiceResponse> garageServices = Collections.emptyList ( );
        log.info ( "-------------->20" );

        String selectedServiceIds = appointmentDetail.getSelectedGarageServiceIds ( ) != null ? appointmentDetail.getSelectedGarageServiceIds ( ) : "";
        log.info ( "-------------->21" );

        if ( selectedServiceIds != null && ! selectedServiceIds.trim ( ).isEmpty ( )
                && ! "null".equalsIgnoreCase ( selectedServiceIds.trim ( ) ) ) {

            garageServices = Arrays.stream ( selectedServiceIds.split ( "," ) )
                    .map ( String::trim )
                    .filter ( id -> ! id.isEmpty ( ) ) // Filter out empty strings
                    .map ( Integer::parseInt )
                    .map ( serviceId -> garageServiceRepository.findById ( serviceId )
                            .map ( garageServiceMapper::entityToResponse )
                            .orElseThrow ( () -> new ResourceNotFoundException ( "Service with id " + serviceId + " not found" ) ) )
                    .toList ( );
        }
        log.info ( "-------------->22" );

        appointmentDetailResponse.setGarageServicesResponse ( garageServices );

        log.info ( "-------------->23" );


        return appointmentDetailResponse;
    }
}
