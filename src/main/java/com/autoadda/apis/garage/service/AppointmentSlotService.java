package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.util.AppointmentStatus;
import com.autoadda.apis.garage.util.DateHelper;
import com.autoadda.apis.garage.util.Status;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSlotService {

    private final GarageWorkingHoursRepository workingHoursRepo;
    private final GarageBreakTimeRepository breakRepo;
    private final GarageHolidayOverrideRepository holidayRepo;
    private final AppointmentDetailsRepository bookingRepo;
    private final GarageRepository garageRepo;
    private final CustomerRepository customerRepo;
    private final AppointmentService appointmentService;

    /**
     * Retrieves available time slots for a garage on a specific date.
     *
     * @param garageId The ID of the garage.
     * @param date     The date for which to retrieve available slots.
     * @return A list of available LocalTime slots.
     */
    @Transactional
    public List<LocalTime> getAvailableSlots(Integer garageId, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek ( );

        Optional<GarageHolidayOverride> overrideOpt = holidayRepo.findByGarageIdAndDate ( garageId, date );
        log.info ( "overrideOpt: {}", overrideOpt.toString ( ) );
        LocalTime openTime, closeTime;
        int slotDuration;

        if ( overrideOpt.isPresent ( ) ) {
            GarageHolidayOverride override = overrideOpt.get ( );
            if ( override.isClosed ( ) )
                return Collections.emptyList ( );
            openTime = override.getOpenTime ( );
            closeTime = override.getCloseTime ( );
            slotDuration = 30;
        } else {
            GarageWorkingHours workingHours = workingHoursRepo.findByGarageIdAndDayOfWeek ( garageId, day )
                    .orElseThrow ( () -> {
                        log.error ( "No working hours defined" );
                        return new ResourceNotFoundException ( "No working hours defined" );
                    } );

            if ( workingHours.isClosed ( ) )
                return Collections.emptyList ( );

            openTime = workingHours.getOpenTime ( );
            closeTime = workingHours.getCloseTime ( );
            slotDuration = workingHours.getSlotDurationMinutes ( );
        }

        List<GarageBreakTime> breaks = breakRepo.findByGarageIdAndDayOfWeek ( garageId, day );
        Set<LocalTime> booked = bookingRepo.findByGarageIdAndScheduleDate ( garageId, date ).stream ( )
                .filter ( b -> b.getStatus ( ) == Status.CONFIRMED ).map ( AppointmentDetails::getServiceStartTime )
                .collect ( Collectors.toSet ( ) );

        List<LocalTime> slots = new ArrayList<> ( );
        for (LocalTime t = openTime; t.plusMinutes ( slotDuration ).compareTo ( closeTime ) <= 0; t = t.plusMinutes ( slotDuration )) {
            LocalTime finalT = t;
            boolean inBreak = breaks.stream ( )
                    .anyMatch ( b -> ! finalT.isBefore ( b.getBreakStart ( ) ) && finalT.isBefore ( b.getBreakEnd ( ) ) );
            if ( ! booked.contains ( t ) && ! inBreak ) {
                slots.add ( t );
            }
        }
        return slots;
    }

    /**
     * Retrieves all available slots for a garage on a specific date.
     *
     * @param garageId The ID of the garage.
     * @param date     The date for which to retrieve all slots.
     * @return A list of LocalTime slots.
     */
    @Transactional
    public AppointmentDetails bookSlot(Integer garageId, Integer vehicleId, LocalDate date, LocalTime time,String autoAddaId) {
        Integer userId = Integer.parseInt ( autoAddaId );
        Optional<AppointmentDetails> existing = bookingRepo.findLockedSlot ( garageId, date, time );
        log.info ( "existing: {}", existing.toString ( ) );
        if ( existing.isPresent ( ) && existing.get ( ).getStatus ( ) == Status.CONFIRMED ) {
            throw new IllegalStateException ( "Slot already booked." );
        }

        Garage garage = garageRepo.findById ( garageId ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) );

        AppointmentDetails appointment = AppointmentDetails.builder ( ).garage ( garage ).scheduleDate ( date )
                .serviceStartTime ( time ).status ( Status.CONFIRMED ).comments ( "Booked via platform" )
                .vehicleId ( vehicleId )
                .customerId ( userId )
                .appointmentReferenceId ( appointmentService.generateAppointmentCode () )
                .insert_tms ( DateHelper.dbDate ( ) ).build ( );

        return bookingRepo.save ( appointment );
    }

    @Transactional
    public void cancelBooking(Integer appointmentId) {
        AppointmentDetails booking = bookingRepo.findById ( appointmentId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "No such booking found to cancel." ) );
        booking.setStatus ( Status.CANCELLED );
        bookingRepo.save ( booking );
    }

    /**
     * Retrieves all bookings made by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of AppointmentDetails for the user's bookings.
     */
    @Transactional
    public List<AppointmentDetails> getBookingsByUser(String userId) {
        Integer user = Integer.parseInt ( userId );
        return bookingRepo.findByCustomerId ( user ).stream ( ).sorted ( Comparator.comparing ( AppointmentDetails::getServiceStartTime ) ).collect ( Collectors.toList ( ) );
    }
@Transactional
    public ResponseEntity<AppointmentDetails> editBooking(Integer garageId, Integer vehicleId, Integer appointmentId, LocalDateTime time, String autoAddaId) {
        AppointmentDetails appointmentDetails = bookingRepo.findById ( appointmentId ).orElseThrow ( () -> new ResourceNotFoundException ( "No such booking found to edit." ) );
        if ( garageId != null && time != null ) {
            LocalDate date = time.toLocalDate ( );
            LocalTime time1 = time.toLocalTime ( );
            Optional<AppointmentDetails> existing = bookingRepo.findLockedSlot ( garageId, date, time1 );
            if ( garageRepo.findById ( garageId ).isEmpty ( ) ) {
                throw new IllegalArgumentException ( "Invalid garage ID." );
            }

            if ( existing.isPresent ( ) && existing.get ( ).getStatus ( ) == Status.CONFIRMED ) {
                throw new IllegalStateException ( "Slot already booked." );
            }
            appointmentDetails.setGarage ( garageRepo.findById ( garageId ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) ) );

        }
        if ( vehicleId != null ) {
            appointmentDetails.setVehicleId ( vehicleId );
        }
        if ( time != null ) {
            appointmentDetails.setServiceStartTime ( time.toLocalTime () );
            appointmentDetails.setScheduleDate ( time.toLocalDate ( ) );
        }
        AppointmentDetails save = bookingRepo.save ( appointmentDetails );
        return new ResponseEntity<> ( save, HttpStatus.OK );

    }

}
