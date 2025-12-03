package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.AppointmentDetailMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.response.AppointmentDetailResponse;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentSlotService {

    private final GarageWorkingHoursRepository workingHoursRepo;
    private final GarageBreakTimeRepository breakRepo;
    private final GarageHolidayOverrideRepository holidayRepo;
    private final AppointmentDetailsRepository bookingRepo;
    private final GarageRepository garageRepo;
    private final AppointmentService appointmentService;
    private final SlotService slotService;
    private final SlotRepository slotRepository;
    private final AppointmentDetailMapper appointmentDetailMapper;
    private final GarageServiceRepository garageServiceRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;

    /**
     * Retrieves available time slots for a garage on a specific date.
     *
     * @param garageId The ID of the garage.
     * @param date     The date for which to retrieve available slots.
     * @return A list of available LocalTime slots.
     */
    @Transactional
    public List<AppointmentSlot> getAvailableSlots(Integer garageId, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek ( );
        // Check if garage is closed (holiday or working hours)
        Optional<GarageHolidayOverride> overrideOpt = holidayRepo.findByGarageIdAndDate ( garageId, date );

        if ( overrideOpt.isPresent ( ) && overrideOpt.get ( ).isClosed ( ) ) {
            return Collections.emptyList ( );
        }

        GarageWorkingHours workingHours = workingHoursRepo.findByGarageIdAndDayOfWeek ( garageId, day )
                .orElseThrow ( () -> new ResourceNotFoundException ( "No working hours defined" ) );

        if ( workingHours.isClosed ( ) ) {
            return Collections.emptyList ( );
        }

        LocalTime openTime = workingHours.getOpenTime ( );
        LocalTime closeTime = workingHours.getCloseTime ( );

        // Get slots for this specific date
        List<AppointmentSlot> slotsForDate = slotRepository.findByGarageIdAndSlotDate ( garageId, date );

        // Filter out slots in break times
        List<GarageBreakTime> breaks = breakRepo.findByGarageIdAndDayOfWeek ( garageId, day );

        return slotsForDate.stream ( )
                .filter ( slot -> slot.getStartTime ( ).compareTo ( openTime ) >= 0 )
                .filter ( slot -> slot.getEndTime ( ).compareTo ( closeTime ) <= 0 )
                .filter ( slot -> ! isSlotInBreak ( slot, breaks ) )
                .filter ( AppointmentSlot::isAvailable )
                .toList ( );
    }

    private boolean isSlotInBreak(AppointmentSlot slot, List<GarageBreakTime> breaks) {
        return breaks.stream ( ).anyMatch ( breakTime ->
                ! slot.getStartTime ( ).isBefore ( breakTime.getBreakStart ( ) ) &&
                        slot.getStartTime ( ).isBefore ( breakTime.getBreakEnd ( ) ) );
    }

    @Transactional
    public List<Map<String, Object>> getAvailableSlotsWithDetails(Integer garageId, LocalDate date) {
        List<AppointmentSlot> availableSlots = getAvailableSlots ( garageId, date );

        return availableSlots.stream ( )
                .map ( slot -> {
                    Map<String, Object> slotInfo = new HashMap<> ( );
                    slotInfo.put ( "slotId", slot.getId ( ) );
                    slotInfo.put ( "startTime", slot.getStartTime ( ) );
                    slotInfo.put ( "endTime", slot.getEndTime ( ) );
                    slotInfo.put ( "maxBookings", slot.getMaxBookings ( ) );
                    slotInfo.put ( "currentBookings", slot.getCurrentBookings ( ) );
                    slotInfo.put ( "availableBookings", slot.getAvailableBookings ( ) );
                    slotInfo.put ( "isAvailable", slot.isAvailable ( ) );
                    return slotInfo;
                } )
                .toList ( );
    }

    /**
     * Retrieves all available slots for a garage on a specific date.
     *
     * @param garageId                 The ID of the garage.
     * @param date                     The date for which to retrieve all slots.
     * @param selectedGarageServiceIds garage
     * @return A list of LocalTime slots.
     */
    @Transactional
    public AppointmentDetailResponse bookSlot(Integer garageId, Integer vehicleId, LocalDate date, LocalTime time, String autoAddaId, String selectedGarageServiceIds) {

        Integer userId = Integer.parseInt ( autoAddaId );

        // Fetch slot with pessimistic write lock to prevent concurrent bookings
        AppointmentSlot slot = slotRepository.findByGarageIdAndStartTime ( garageId, date, time )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Slot not available or fully booked" ) );

        // Double-check availability (should always be true due to query condition, but defensive programming)
        if ( ! slot.isAvailable ( ) ) {
            throw new IllegalStateException ( "Slot is fully booked." );
        }

        Garage garage = garageRepo.findById ( garageId ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) );

        // Increment bookings immediately within the same transaction
        slot.setCurrentBookings ( slot.getCurrentBookings ( ) + 1 );
        slotRepository.save ( slot );

        AppointmentDetails appointment = AppointmentDetails.builder ( ).garage ( garage ).scheduleDate ( date )
                .serviceStartTime ( time ).status ( Status.CONFIRMED ).comments ( "Booked via platform" )
                .vehicleId ( vehicleId )
                .customerId ( userId )
                .selectedGarageServiceIds ( selectedGarageServiceIds )
                .appointmentReferenceId ( appointmentService.generateAppointmentCode ( ) )
                .slot ( slot )
                .insertTms ( DateHelper.dbDate ( ) ).build ( );

        AppointmentDetails save = bookingRepo.save ( appointment );
        generateAppointmentServices ( save, selectedGarageServiceIds );
        return appointmentDetailMapper.entityToResponse ( save );
    }

    private void generateAppointmentServices(AppointmentDetails appointment, String selectedGarageServiceIds) {
        List<Integer> serviceIds = Arrays.stream ( selectedGarageServiceIds.split ( "," ) )
                .map ( Integer::parseInt )
                .toList ( );

        List<GarageService> services = garageServiceRepository.findAllById ( serviceIds );

        List<AppointmentServices> appointmentServices = services.stream ( )
                .map ( service -> AppointmentServices.builder ( )
                        .appointmentId ( appointment.getId ( ) )
                        .service ( service )
                        .price ( service.getPrice ( ) )
                        .labourPrice ( service.getLabourPrice ( ) )
                        .status ( Status.PENDING )
                        .build ( ) )
                .toList ( );

        appointmentServiceRepository.saveAll ( appointmentServices );

    }

    @Transactional
    public void cancelBooking(Integer appointmentId) {
        AppointmentDetails booking = bookingRepo.findById ( appointmentId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "No such booking found to cancel." ) );

        // Only decrement if booking was previously confirmed and has an associated slot
        if ( booking.getStatus ( ) == Status.CONFIRMED && booking.getSlot ( ) != null ) {
            slotService.decrementSlotBookings ( booking.getSlot ( ).getId ( ) );
        }

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
    public List<AppointmentDetailResponse> getBookingsByUser(String userId) {
        Integer user = Integer.parseInt ( userId );
        List<AppointmentDetails> list = bookingRepo.findByCustomerId ( user )
                .stream ( )
                .sorted ( Comparator.comparing ( AppointmentDetails::getServiceStartTime )
                        .reversed ( ) )
                .toList ( );
        return list.stream ( ).map ( appointmentDetailMapper::entityToResponse ).toList ( );
    }

    @Transactional
    public ResponseEntity<AppointmentDetailResponse> editBooking(Integer garageId, Integer vehicleId, Integer appointmentId, LocalDateTime time, String autoAddaId, Status status) {
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

        if ( status != null ) {
            appointmentDetails.setStatus ( status );
        }

        if ( vehicleId != null ) {
            appointmentDetails.setVehicleId ( vehicleId );
        }
        if ( time != null ) {
            appointmentDetails.setServiceStartTime ( time.toLocalTime ( ) );
            appointmentDetails.setScheduleDate ( time.toLocalDate ( ) );
        }
        AppointmentDetailResponse save = appointmentDetailMapper.entityToResponse ( bookingRepo.save ( appointmentDetails ) );

        return new ResponseEntity<> ( save, HttpStatus.OK );

    }


}
