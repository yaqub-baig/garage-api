package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.response.AppointmentSlotResponse;
import org.springframework.stereotype.Component;

@Component
public class SlotMapper {

    public AppointmentSlotResponse toResponse(AppointmentSlot slot) {
        AppointmentSlotResponse response = new AppointmentSlotResponse ( );
        response.setId ( slot.getId ( ) );
        response.setSlotDate ( slot.getSlotDate ( ) );
        response.setStartTime ( slot.getStartTime ( ) );
        response.setEndTime ( slot.getEndTime ( ) );
        response.setMaxBookings ( slot.getMaxBookings ( ) );
        response.setCurrentBookings ( slot.getCurrentBookings ( ) );
        response.setAvailableBookings ( slot.getAvailableBookings ( ) );
        response.setAvailable ( slot.isAvailable ( ) );
        response.setGarageId ( slot.getGarage ( ).getId ( ) );
        return response;
    }

}
