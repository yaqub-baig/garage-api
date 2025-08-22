package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.SlotRepository;
import com.autoadda.apis.garage.request.AppointmentSlotRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;

    public AppointmentSlot createSlot(AppointmentSlotRequest request) {
        AppointmentSlot appointmentSlot = new AppointmentSlot ( );
        appointmentSlot.setStartTime ( request.getStartTime ( ) );
        appointmentSlot.setEndTime ( request.getEndTime ( ) );
        appointmentSlot.setMaxBookings ( request.getMaxBookings ( ) );
        appointmentSlot.setCurrentBookings ( 0 );
        return slotRepository.save ( appointmentSlot );
    }

    public List<AppointmentSlot> getAllSlots() {
        return slotRepository.findAll ( );
    }

    public AppointmentSlot updateSlot(Integer id, AppointmentSlotRequest request) {
        AppointmentSlot appointmentSlot = slotRepository.findById ( id ).orElseThrow ( );
        if ( request.getStartTime ( ) != null )
            appointmentSlot.setStartTime ( request.getStartTime ( ) );

        if ( request.getEndTime ( ) != null )
            appointmentSlot.setEndTime ( request.getEndTime ( ) );

        if ( request.getCurrentBookings ( ) != null )
            appointmentSlot.setCurrentBookings ( request.getCurrentBookings ( ) );

        if ( request.getMaxBookings ( ) != null )
            appointmentSlot.setMaxBookings ( request.getMaxBookings ( ) );

        return slotRepository.save ( appointmentSlot );
    }

    public void deleteSlot(Integer id) {
        slotRepository.deleteById ( id );
    }

    public AppointmentSlot getSlotById(Integer id) {
        return slotRepository.findById ( id ).orElseThrow ( ()->new ResourceNotFoundException ( "slot not found" ) );
    }
    public List<AppointmentSlot> getAvailableSlots(LocalTime startTime, LocalTime endTime) {
        return slotRepository.findByStartTimeAndEndTimeAndCurrentBookingsLessThanMaxBookings(startTime, endTime);
    }

    @Transactional
    public void incrementSlotBookings(Integer slotId) {
        AppointmentSlot slot = getSlotById(slotId);
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Slot is fully booked");
        }
        slot.setCurrentBookings(slot.getCurrentBookings() + 1);
        slotRepository.save(slot);
    }

    @Transactional
    public void decrementSlotBookings(Integer slotId) {
        AppointmentSlot slot = getSlotById(slotId);
        if (slot.getCurrentBookings() > 0) {
            slot.setCurrentBookings(slot.getCurrentBookings() - 1);
            slotRepository.save(slot);
        }
    }
}
