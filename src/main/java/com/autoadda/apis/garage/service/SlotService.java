package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.SlotMapper;
import com.autoadda.apis.garage.repository.SlotRepository;
import com.autoadda.apis.garage.request.AppointmentSlotRequest;
import com.autoadda.apis.garage.response.AppointmentSlotResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final SlotMapper slotMapper;

    public AppointmentSlotResponse createSlot(AppointmentSlotRequest request) {
        AppointmentSlot appointmentSlot = new AppointmentSlot ( );
        appointmentSlot.setStartTime ( request.getStartTime ( ) );
        appointmentSlot.setEndTime ( request.getEndTime ( ) );
        appointmentSlot.setMaxBookings ( request.getMaxBookings ( ) );
        appointmentSlot.setCurrentBookings ( 0 );
        return slotMapper.toResponse ( slotRepository.save ( appointmentSlot ) );
    }

    public List<AppointmentSlotResponse> getAllSlots() {
        return slotRepository.findAll ( ).stream ( ).map ( slotMapper::toResponse ).toList ( );
    }

    public AppointmentSlotResponse updateSlot(Integer id, AppointmentSlotRequest request) {
        AppointmentSlot appointmentSlot = slotRepository.findById ( id ).orElseThrow ( );
        if ( request.getStartTime ( ) != null )
            appointmentSlot.setStartTime ( request.getStartTime ( ) );

        if ( request.getEndTime ( ) != null )
            appointmentSlot.setEndTime ( request.getEndTime ( ) );

        if ( request.getCurrentBookings ( ) != null )
            appointmentSlot.setCurrentBookings ( request.getCurrentBookings ( ) );

        if ( request.getMaxBookings ( ) != null )
            appointmentSlot.setMaxBookings ( request.getMaxBookings ( ) );

        return slotMapper.toResponse ( slotRepository.save ( appointmentSlot ) );
    }

    public void deleteSlot(Integer id) {
        slotRepository.deleteById ( id );
    }

    public AppointmentSlotResponse getSlotById(Integer id) {
        AppointmentSlot slot = slotRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "slot not found" ) );
        return slotMapper.toResponse ( slot );
    }

    public List<AppointmentSlotResponse> getAvailableSlots(LocalTime startTime, LocalTime endTime) {
        return slotRepository.findAvailableSlotsBetween ( startTime, endTime ).stream ( ).map ( slotMapper::toResponse ).toList ( );
    }

    @Transactional
    public void incrementSlotBookings(Integer slotId) {
        AppointmentSlot slot = slotRepository.findById ( slotId ).orElseThrow ( () -> new ResourceNotFoundException ( "slot not found" ) );
        if ( ! slot.isAvailable ( ) ) {
            throw new IllegalStateException ( "Slot is fully booked" );
        }
        slot.setCurrentBookings ( slot.getCurrentBookings ( ) + 1 );
        slotRepository.save ( slot );
    }

    @Transactional
    public void decrementSlotBookings(Integer slotId) {
        AppointmentSlot slot = slotRepository.findById ( slotId ).orElseThrow ( () -> new ResourceNotFoundException ( "slot not found" ) );
        if ( slot.getCurrentBookings ( ) > 0 ) {
            slot.setCurrentBookings ( slot.getCurrentBookings ( ) - 1 );
            slotRepository.save ( slot );
        }
    }

    public AppointmentSlot findAvailableSlotByGarageIdAndTime(Integer garageId, LocalDate date, LocalTime time) {
        return slotRepository
                .findByGarageIdAndStartTime ( garageId, date, time )
                .orElseThrow ( () -> new ResourceNotFoundException ( "slot not found" ) );


    }
}
