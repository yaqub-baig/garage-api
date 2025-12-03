package com.autoadda.apis.garage.service.os;

import com.autoadda.apis.garage.entity.os.*;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.os.OsAppointmentDetailMapper;
import com.autoadda.apis.garage.repository.CustomerRepository;
import com.autoadda.apis.garage.repository.os.*;
import com.autoadda.apis.garage.response.os.OsAppointmentDetailResponse;
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
public class OsAppointmentSlotService {

    private final OsWorkHourRepository osWorkingHoursRepo;
    private final OsBreakTimeRepository breakRepo;
    private final OsHolidayOverrideRepository holidayRepo;
    private final OsAppointmentDetailsRepository bookingRepo;
    private final OtherServiceRepository otherService;
    private final CustomerRepository customerRepo;
    private final OsAppointmentService appointmentService;
    private final OsSlotService slotService;
    private final OsSlotRepository slotRepository;
    private final OsAppointmentDetailMapper appointmentDetailMapper;
    private final OsServiceRepository osServiceRepository;
    private final OsAppointmentServiceRepository appointmentServiceRepository;
    private final com.autoadda.apis.garage.service.VehicleBookingValidationService vehicleBookingValidationService;

    /**
     * Retrieves available time slots for a garage on a specific date.
     *
     * @param otherServiceId The ID of the garage.
     * @param date     The date for which to retrieve available slots.
     * @return A list of available LocalTime slots.
     */
    @Transactional
    public List<OsAppointmentSlot> getAvailableSlots(Integer otherServiceId, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek ( );
//TODO:1
        // Check if garage is closed (holiday or working hours)
        Optional<OsHolidayOverride> overrideOpt = holidayRepo.findByOtherServiceIdAndDate (otherServiceId, date );

        if ( overrideOpt.isPresent ( ) && overrideOpt.get ( ).isClosed ( ) ) {
            return Collections.emptyList ( );
        }

        OsWorkHours workingHours = osWorkingHoursRepo.findByOtherServiceIdAndDayOfWeek (otherServiceId, day )
                .orElseThrow ( () -> new ResourceNotFoundException ( "No working hours defined" ) );

        if ( workingHours.isClosed ( ) ) {
            return Collections.emptyList ( );
        }

        LocalTime openTime = workingHours.getOpenTime ( );
        LocalTime closeTime = workingHours.getCloseTime ( );

        // Get slots for this specific date
        List<OsAppointmentSlot> slotsForDate = slotRepository.findByOtherServiceIdAndSlotDate (otherServiceId, date );

        // Filter out slots in break times
        List<OsBreakTime> breaks = breakRepo.findByOtherServiceIdAndDayOfWeek ( otherServiceId, day );

        return slotsForDate.stream ( )
                .filter ( slot -> slot.getStartTime ( ).compareTo ( openTime ) >= 0 )
                .filter ( slot -> slot.getEndTime ( ).compareTo ( closeTime ) <= 0 )
                .filter ( slot -> ! isSlotInBreak ( slot, breaks ) )
                .filter ( OsAppointmentSlot::isAvailable )
                .toList ( );
    }

    private boolean isSlotInBreak(OsAppointmentSlot slot, List<OsBreakTime> breaks) {
        return breaks.stream ( ).anyMatch ( breakTime ->
                ! slot.getStartTime ( ).isBefore ( breakTime.getBreakStart ( ) ) &&
                        slot.getStartTime ( ).isBefore ( breakTime.getBreakEnd ( ) ) );
    }

    @Transactional
    public List<Map<String, Object>> getAvailableSlotsWithDetails(Integer garageId, LocalDate date) {
        List<OsAppointmentSlot> availableSlots = getAvailableSlots ( garageId, date );

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
     * @param otherServiceId                 The ID of the garage.
     * @param date                     The date for which to retrieve all slots.
     * @param selectedOtherServiceIds
     * @return A list of LocalTime slots.
     */
    @Transactional
    public OsAppointmentDetailResponse bookSlot(Integer otherServiceId, Integer vehicleId, LocalDate date, LocalTime time, String autoAddaId, String selectedOtherServiceIds) {

        Integer userId = Integer.parseInt ( autoAddaId );
        OsAppointmentSlot slot = slotService.findAvailableSlotByGarageIdAndTime ( otherServiceId, date, time );

        if ( ! slot.isAvailable ( ) ) {
            throw new IllegalStateException ( "Slot is Fully booked." );
        }

        OtherService otherService1 = otherService.findById( otherServiceId ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) );

        // Validate vehicle is not already booked at this date/time
        vehicleBookingValidationService.validateVehicleAvailability ( vehicleId, date, time );

        OsAppointmentDetails appointment = OsAppointmentDetails.builder ( ).otherService ( otherService1 ).scheduleDate ( date )
                .serviceStartTime ( time ).status ( Status.CONFIRMED ).comments ( "Booked via platform" )
                .vehicleId ( vehicleId )
                .customerId ( userId )
                .selectedOtherServiceIds ( selectedOtherServiceIds )
                .appointmentReferenceId ( appointmentService.generateAppointmentCode ( ) )
                .slot ( slot )
                .insertTms( DateHelper.dbDate ( ) ).build ( );
        slotService.incrementSlotBookings ( slot.getId ( ) );
        OsAppointmentDetails save = bookingRepo.save ( appointment );
        generateAppointmentServices(save, selectedOtherServiceIds);
        return appointmentDetailMapper.entityToResponse (save);
    }

    private void generateAppointmentServices(OsAppointmentDetails appointment, String selectedOtherServiceIds) {
        List<Integer> serviceIds = Arrays.stream ( selectedOtherServiceIds.split ( "," ) )
                .map ( Integer::parseInt )
                .toList ( ) ;

        List<OsService> services = osServiceRepository.findAllById ( serviceIds );

        List<com.autoadda.apis.garage.entity.os.OsAppointmentServices> appointmentServices = services.stream ( )
                .map ( service -> com.autoadda.apis.garage.entity.os.OsAppointmentServices.builder ( )
                        .appointmentId ( appointment.getId () )
                        .osService( service )
                        .status ( Status.PENDING )
                        .build ( ) )
                .toList ( ) ;

        appointmentServiceRepository.saveAll ( appointmentServices );

    }

    @Transactional
    public void cancelBooking(Integer appointmentId) {
        OsAppointmentDetails booking = bookingRepo.findById ( appointmentId )
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
    public List<OsAppointmentDetailResponse> getBookingsByUser(String userId) {
        Integer user = Integer.parseInt ( userId );
        List<OsAppointmentDetails> list = bookingRepo.findByCustomerId ( user )
                .stream ( )
                .sorted ( Comparator.comparing ( OsAppointmentDetails::getServiceStartTime )
                        .reversed ( ) )
                .toList ( );
        return list.stream ().map(appointmentDetailMapper::entityToResponse).toList ();
    }

    @Transactional
    public ResponseEntity<OsAppointmentDetails> editBooking(Integer otherServiceId, Integer vehicleId, Integer appointmentId, LocalDateTime time, String autoAddaId) {
        OsAppointmentDetails appointmentDetails = bookingRepo.findById ( appointmentId ).orElseThrow ( () -> new ResourceNotFoundException ( "No such booking found to edit." ) );
        if ( otherServiceId != null && time != null ) {
            LocalDate date = time.toLocalDate ( );
            LocalTime time1 = time.toLocalTime ( );
            Optional<OsAppointmentDetails> existing = bookingRepo.findLockedSlot ( otherServiceId, date, time1 );
            if ( otherService.findById ( otherServiceId ).isEmpty ( ) ) {
                throw new IllegalArgumentException ( "Invalid garage ID." );
            }

            if ( existing.isPresent ( ) && existing.get ( ).getStatus ( ) == Status.CONFIRMED ) {
                throw new IllegalStateException ( "Slot already booked." );
            }

            // Validate vehicle availability for reschedule
            Integer targetVehicleId = vehicleId != null ? vehicleId : appointmentDetails.getVehicleId ( );
            vehicleBookingValidationService.validateVehicleAvailabilityForEdit (
                    targetVehicleId, date, time1, appointmentId, false );

            appointmentDetails.setOtherService ( otherService.findById ( otherServiceId ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) ) );

        }
        if ( vehicleId != null ) {
            appointmentDetails.setVehicleId ( vehicleId );
        }
        if ( time != null ) {
            appointmentDetails.setServiceStartTime ( time.toLocalTime ( ) );
            appointmentDetails.setScheduleDate ( time.toLocalDate ( ) );
        }
        OsAppointmentDetails save = bookingRepo.save ( appointmentDetails );
        return new ResponseEntity<> ( save, HttpStatus.OK );

    }


}
