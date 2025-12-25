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
    private final VehicleBookingValidationService vehicleBookingValidationService;
    private final SubscriptionValidationService subscriptionValidationService;
    private final GarageSubscriptionRepository garageSubscriptionRepository;

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
     * @param vehicleOwnerSubscriptionId Optional subscription ID to use for this booking
     * @return A list of LocalTime slots.
     */
    @Transactional
    public AppointmentDetailResponse bookSlot(Integer garageId, Integer vehicleId, LocalDate date, LocalTime time, String autoAddaId, String selectedGarageServiceIds, Integer vehicleOwnerSubscriptionId) {

        // Validate vehicle is not already booked at this date/time
        vehicleBookingValidationService.validateVehicleAvailability ( vehicleId, date, time );

        Integer userId = Integer.parseInt ( autoAddaId );

        // Fetch slot with pessimistic write lock to prevent concurrent bookings
        AppointmentSlot slot = slotRepository.findByGarageIdAndStartTime ( garageId, date, time )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Slot not available or fully booked" ) );

        // Double-check availability (should always be true due to query condition, but defensive programming)
        if ( ! slot.isAvailable ( ) ) {
            throw new IllegalStateException ( "Slot is fully booked." );
        }

        Garage garage = garageRepo.findById ( garageId ).orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) );


        // Validate and apply subscription if provided
        VehicleOwnerSubscription subscription = null;
        if (vehicleOwnerSubscriptionId != null) {
            subscription = subscriptionValidationService.validateSubscriptionForBooking(
                    vehicleOwnerSubscriptionId,
                    userId,
                    garageId,
                    selectedGarageServiceIds
            );
        }

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
                .garageSubscriptionPlanId ( subscription != null ? subscription.getGarageSubscription().getId().toString() : null )
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
    public ResponseEntity<AppointmentDetailResponse> editBooking(Integer garageId, Integer vehicleId, Integer appointmentId, LocalDateTime time, String autoAddaId, Status status, String selectedGarageServiceIds) {
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

            // Validate vehicle availability for reschedule
            Integer targetVehicleId = vehicleId != null ? vehicleId : appointmentDetails.getVehicleId ( );
            vehicleBookingValidationService.validateVehicleAvailabilityForEdit (
                    targetVehicleId, date, time1, appointmentId, true );

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

        // Handle service updates with subscription awareness
        if (selectedGarageServiceIds != null && !selectedGarageServiceIds.trim().isEmpty()) {
            handleServiceUpdate(appointmentDetails, selectedGarageServiceIds);
        }

        AppointmentDetailResponse save = appointmentDetailMapper.entityToResponse ( bookingRepo.save ( appointmentDetails ) );

        return new ResponseEntity<> ( save, HttpStatus.OK );

    }

    /**
     * Updates AppointmentServices records for an appointment.
     * Deletes existing records and creates new ones based on updated service list.
     *
     * @param appointment The appointment being updated
     * @param newServiceIds Comma-separated GarageService IDs
     */
    private void updateAppointmentServicesRecords(AppointmentDetails appointment, String newServiceIds) {
        // Step 1: Delete existing AppointmentServices records
        List<AppointmentServices> existingServices =
            appointmentServiceRepository.findByAppointmentId(appointment.getId());

        if (!existingServices.isEmpty()) {
            appointmentServiceRepository.deleteAll(existingServices);
            log.info("Deleted {} existing appointment services for appointment {}",
                existingServices.size(), appointment.getId());
        }

        // Step 2: Generate new AppointmentServices records
        // Reuse existing generateAppointmentServices method
        generateAppointmentServices(appointment, newServiceIds);

        log.info("Created new appointment services for appointment {}", appointment.getId());
    }

    /**
     * Finds the VehicleOwnerSubscription for an appointment.
     * Since AppointmentDetails stores garageSubscriptionPlanId (GarageSubscription ID),
     * we need to find the VehicleOwnerSubscription that references this plan and belongs to the customer.
     *
     * @param appointment The appointment
     * @param subscriptionPlan The GarageSubscription plan
     * @return The VehicleOwnerSubscription, or null if not found
     */
    private VehicleOwnerSubscription findVehicleOwnerSubscriptionForAppointment(
            AppointmentDetails appointment, GarageSubscription subscriptionPlan) {

        // Use the subscription validation service to get active subscriptions for this garage
        List<VehicleOwnerSubscription> activeSubscriptions =
            subscriptionValidationService.getActiveSubscriptionsForGarage(
                appointment.getCustomerId(),
                appointment.getGarage().getId()
            );

        // Find the one matching this subscription plan
        return activeSubscriptions.stream()
            .filter(sub -> sub.getGarageSubscription().getId().equals(subscriptionPlan.getId()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Validates if new services are compatible with the subscription plan.
     * Returns true if subscription should be kept, false if it should be removed.
     *
     * @param appointment The appointment with subscription
     * @param newServiceIds Comma-separated GarageService IDs
     * @param subscriptionPlanId The GarageSubscription ID
     * @return true if all services are covered and within limits, false otherwise
     */
    private boolean validateServicesAgainstSubscription(
            AppointmentDetails appointment, String newServiceIds, String subscriptionPlanId) {

        // Step 1: Parse new service IDs
        List<Integer> garageServiceIds = Arrays.stream(newServiceIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Integer::parseInt)
            .toList();

        if (garageServiceIds.isEmpty()) {
            log.warn("No services provided for appointment {}", appointment.getId());
            return false;
        }

        // Step 2: Fetch GarageService entities
        List<GarageService> garageServices = garageServiceRepository.findAllById(garageServiceIds);

        if (garageServices.size() != garageServiceIds.size()) {
            log.warn("Some garage services not found for IDs: {}", garageServiceIds);
            return false;
        }

        // Step 3: Convert GarageService IDs to AvailableService IDs
        List<Integer> availableServiceIds = garageServices.stream()
            .map(gs -> {
                if (gs.getAvailableService() == null) {
                    throw new IllegalStateException("GarageService " + gs.getId() + " has no AvailableService");
                }
                return gs.getAvailableService().getId();
            })
            .toList();

        // Step 4: Fetch subscription plan
        GarageSubscription subscriptionPlan;
        try {
            Integer planId = Integer.parseInt(subscriptionPlanId);
            subscriptionPlan = garageSubscriptionRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Subscription plan not found: " + subscriptionPlanId));
        } catch (NumberFormatException e) {
            log.error("Invalid subscription plan ID format: {}", subscriptionPlanId);
            return false;
        }

        // Step 5: Get services included in subscription
        List<Integer> includedServiceIds = subscriptionPlan.getServicesIncluded().stream()
            .map(AvailableService::getId)
            .toList();

        // Step 6: Check if ALL new services are in the subscription plan
        for (Integer availableServiceId : availableServiceIds) {
            if (!includedServiceIds.contains(availableServiceId)) {
                log.info("Service {} not in subscription plan {}. Will remove subscription.",
                    availableServiceId, subscriptionPlanId);
                return false;
            }
        }

        // Step 7: Re-validate usage limits using SubscriptionValidationService
        // This will throw ConflictException if limits are exceeded
        try {
            // Build comma-separated string of AvailableService IDs for validation
            String availableServiceIdsStr = availableServiceIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));

            // Find the VehicleOwnerSubscription for this customer
            VehicleOwnerSubscription vehicleOwnerSubscription =
                findVehicleOwnerSubscriptionForAppointment(appointment, subscriptionPlan);

            if (vehicleOwnerSubscription == null) {
                log.warn("No VehicleOwnerSubscription found for appointment {}", appointment.getId());
                return false;
            }

            // Validate subscription for the new services
            subscriptionValidationService.validateSubscriptionForBooking(
                vehicleOwnerSubscription.getId(),
                appointment.getCustomerId(),
                appointment.getGarage().getId(),
                availableServiceIdsStr
            );

            log.info("Services validated successfully against subscription {}", subscriptionPlanId);
            return true;

        } catch (Exception e) {
            // Usage limits exceeded or other validation failure
            log.warn("Subscription validation failed for appointment {}: {}",
                appointment.getId(), e.getMessage());
            throw e; // Re-throw to inform user
        }
    }

    /**
     * Handles service updates with subscription-aware logic.
     * - Validates new services against existing subscription
     * - Auto-removes subscription if services are not covered
     * - Re-validates usage limits if subscription is maintained
     * - Updates AppointmentServices records
     *
     * @param appointment The appointment being updated
     * @param newServiceIds Comma-separated GarageService IDs
     */
    private void handleServiceUpdate(AppointmentDetails appointment, String newServiceIds) {
        log.info("Handling service update for appointment {}, new services: {}",
            appointment.getId(), newServiceIds);

        // Step 1: Check if appointment has an active subscription
        String currentSubscriptionPlanId = appointment.getGarageSubscriptionPlanId();

        if (currentSubscriptionPlanId != null && !currentSubscriptionPlanId.trim().isEmpty()) {
            // Appointment has subscription - validate if it should be kept or removed
            boolean shouldKeepSubscription = validateServicesAgainstSubscription(
                appointment, newServiceIds, currentSubscriptionPlanId);

            if (!shouldKeepSubscription) {
                // Auto-remove subscription
                log.info("Auto-removing subscription {} from appointment {} due to service change",
                    currentSubscriptionPlanId, appointment.getId());
                appointment.setGarageSubscriptionPlanId(null);
            }
        }

        // Step 2: Update the selectedGarageServiceIds field
        appointment.setSelectedGarageServiceIds(newServiceIds);

        // Step 3: Update AppointmentServices records
        updateAppointmentServicesRecords(appointment, newServiceIds);
    }


}
