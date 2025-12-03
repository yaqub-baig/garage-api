package com.autoadda.apis.garage.service.os;

import com.autoadda.apis.garage.entity.os.OsAppointmentSlot;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.os.OsSlotMapper;
import com.autoadda.apis.garage.repository.os.OsSlotRepository;
import com.autoadda.apis.garage.request.os.OsAppointmentSlotRequest;
import com.autoadda.apis.garage.response.os.OsAppointmentSlotResponse;
import com.autoadda.apis.garage.response.os.OsMonthlyAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OsSlotService {
    public static final String SLOT_NOT_FOUND = "slot not found";
    private final OsSlotRepository slotRepository;
    private final OsSlotMapper slotMapper;

    public OsAppointmentSlotResponse createSlot(OsAppointmentSlotRequest request) {
        OsAppointmentSlot appointmentSlot = new OsAppointmentSlot();
        appointmentSlot.setStartTime(request.getStartTime());
        appointmentSlot.setEndTime(request.getEndTime());
        appointmentSlot.setMaxBookings(request.getMaxBookings());
        appointmentSlot.setCurrentBookings(0);

        return slotMapper.toResponse(slotRepository.save(appointmentSlot));
    }

    public List<OsAppointmentSlotResponse> getAllSlots() {
        return slotRepository.findAll ( ).stream ( ).map ( slotMapper::toResponse ).toList ( );
    }

    public OsAppointmentSlotResponse updateSlot(Integer id, OsAppointmentSlotRequest request) {
        OsAppointmentSlot appointmentSlot = slotRepository.findById ( id ).orElseThrow ( );
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

    public OsAppointmentSlotResponse getSlotById(Integer id) {
        OsAppointmentSlot slot = slotRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException (SLOT_NOT_FOUND) );
        return slotMapper.toResponse ( slot );
    }

    public List<OsAppointmentSlotResponse> getAvailableSlots(LocalTime startTime, LocalTime endTime) {
        return slotRepository.findAvailableSlotsBetween ( startTime, endTime ).stream ( ).map ( slotMapper::toResponse ).toList ( );
    }

    public List<OsAppointmentSlotResponse> getAvailableSlots(Integer otherServiceId) {
        return slotRepository.findAvailableSlotsByOtherServiceId ( otherServiceId ).stream ( ).filter ( OsAppointmentSlot::isAvailable ).map ( slotMapper::toResponse ).toList ( );
    }

    @Transactional
    public void incrementSlotBookings(Integer slotId) {
        OsAppointmentSlot slot = slotRepository.findById ( slotId ).orElseThrow ( () -> new ResourceNotFoundException (SLOT_NOT_FOUND) );
        if ( ! slot.isAvailable ( ) ) {
            throw new IllegalStateException ( "Slot is fully booked" );
        }
        slot.setCurrentBookings ( slot.getCurrentBookings ( ) + 1 );
        slotRepository.save ( slot );
    }

    @Transactional
    public void decrementSlotBookings(Integer slotId) {
        OsAppointmentSlot slot = slotRepository.findById ( slotId ).orElseThrow ( () -> new ResourceNotFoundException (SLOT_NOT_FOUND) );
        if ( slot.getCurrentBookings ( ) > 0 ) {
            slot.setCurrentBookings ( slot.getCurrentBookings ( ) - 1 );
            slotRepository.save ( slot );
        }
    }

    public OsAppointmentSlot findAvailableSlotByGarageIdAndTime(Integer otherServiceId, LocalDate date, LocalTime time) {
        return slotRepository
                .findByOtherServiceAndStartTime ( otherServiceId, date, time )
                .orElseThrow ( () -> new ResourceNotFoundException ( "either slot not found or slot is fully booked" ) );
    }


    public OsMonthlyAvailabilityResponse getMonthlyAvailability(Integer otherServiceId) {
        LocalDate today = LocalDate.now ( );
        // Start from tomorrow (current date + 1)
        LocalDate startDate = today;

        // End date is 2 months from today
        LocalDate endDate = today.plusMonths ( 2 );

        int year = startDate.getYear ( );
        int month = startDate.getMonthValue ( );
        // Get all slots for the garage in this month
        List<OsAppointmentSlot> monthlySlots = slotRepository
                .findMonthlySlots ( otherServiceId, startDate, endDate );

        // Group slots by date
        Map<LocalDate, List<OsAppointmentSlot>> slotsByDate = monthlySlots.stream ( )
                .collect ( Collectors.groupingBy ( OsAppointmentSlot::getSlotDate ) );

        // Create daily availability map
        Map<LocalDate, Boolean> dailyAvailability = new HashMap<> ( );

        // Check each day of the month
        for (LocalDate date = startDate;
             ! date.isAfter ( endDate );
             date = date.plusDays ( 1 )) {

            List<OsAppointmentSlot> daySlots = slotsByDate.get ( date );

            if ( daySlots == null || daySlots.isEmpty ( ) ) {
                // No slots for this day
                dailyAvailability.put ( date, false );
            } else {
                // Check if any slot has available appointments
                boolean hasAvailableSlots = daySlots.stream ( )
                        .anyMatch ( OsAppointmentSlot::isAvailable );
                dailyAvailability.put ( date, hasAvailableSlots );
            }
        }

        return OsMonthlyAvailabilityResponse.builder ( )
                .otherServiceId ( otherServiceId )
                .year ( year )
                .month ( month )
                .dailyAvailability ( dailyAvailability )
                .build ( );
    }

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void markPastSlotsAsUnavailable() {
        LocalDateTime now = LocalDateTime.now ( );
        LocalDate today = now.toLocalDate ( );
        LocalTime currentTime = now.toLocalTime ( );

        // Find slots that have passed
        List<OsAppointmentSlot> pastSlots = slotRepository.findPastSlots ( today, currentTime );

        // Mark them as full to prevent new bookings
        pastSlots.forEach ( slot -> {
            if ( slot.getCurrentBookings ( ) == 0 ) {
                slot.setCurrentBookings ( slot.getMaxBookings ( ) ); // Make it appear full
            }
        } );
        slotRepository.saveAll ( pastSlots );
    }
}
