package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Service responsible for validating vehicle booking constraints.
 * Prevents duplicate bookings across all garages.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleBookingValidationService {

    private final AppointmentDetailsRepository appointmentDetailsRepository;

    /**
     * Validates that a vehicle is not already booked at the specified date/time.
     *
     * @param vehicleId        Vehicle ID to check
     * @param scheduleDate     Date of the appointment
     * @param serviceStartTime Start time of the service
     * @throws ConflictException if vehicle already has a confirmed booking at this time
     */
    public void validateVehicleAvailability(Integer vehicleId, LocalDate scheduleDate, LocalTime serviceStartTime) {
        log.debug("Validating vehicle availability: vehicleId={}, date={}, time={}",
                vehicleId, scheduleDate, serviceStartTime);

        boolean hasGarageBooking = appointmentDetailsRepository
                .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                        vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED);

        if (hasGarageBooking) {
            Optional<AppointmentDetails> conflictingBooking = appointmentDetailsRepository
                    .findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED);

            String garageName = conflictingBooking
                    .map(booking -> booking.getGarage().getName())
                    .orElse("Unknown Garage");

            log.warn("Duplicate booking attempt: Vehicle {} already booked at {} {} in garage {}",
                    vehicleId, scheduleDate, serviceStartTime, garageName);

            throw new ConflictException(
                    String.format("Vehicle is already booked at %s on %s at garage '%s'. " +
                                    "A vehicle cannot be at multiple locations at the same time.",
                            serviceStartTime, scheduleDate, garageName)
            );
        }

        // TODO: OS module was extracted to its own service. Re-add the cross-check
        // against OtherService appointments via a REST call to the OS microservice.

        log.debug("Vehicle {} is available at {} {}", vehicleId, scheduleDate, serviceStartTime);
    }

    /**
     * Validates vehicle availability during reschedule/edit operations.
     * Excludes the current appointment being edited from conflict checks.
     *
     * @param vehicleId            Vehicle ID
     * @param scheduleDate         New date
     * @param serviceStartTime     New time
     * @param currentAppointmentId Current appointment ID to exclude
     * @throws ConflictException if vehicle has conflicting booking
     */
    public void validateVehicleAvailabilityForEdit(
            Integer vehicleId,
            LocalDate scheduleDate,
            LocalTime serviceStartTime,
            Integer currentAppointmentId) {

        log.debug("Validating vehicle availability for edit: vehicleId={}, date={}, time={}, currentAppointmentId={}",
                vehicleId, scheduleDate, serviceStartTime, currentAppointmentId);

        boolean hasGarageConflict = appointmentDetailsRepository
                .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatusAndIdNot(
                        vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED, currentAppointmentId);

        if (hasGarageConflict) {
            Optional<AppointmentDetails> conflictingBooking = appointmentDetailsRepository
                    .findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED)
                    .filter(booking -> !booking.getId().equals(currentAppointmentId));

            String garageName = conflictingBooking
                    .map(booking -> booking.getGarage().getName())
                    .orElse("Unknown Garage");

            log.warn("Edit conflict: Vehicle {} already booked at {} {} in garage {}",
                    vehicleId, scheduleDate, serviceStartTime, garageName);

            throw new ConflictException(
                    String.format("Cannot reschedule: Vehicle is already booked at %s on %s at garage '%s'.",
                            serviceStartTime, scheduleDate, garageName)
            );
        }

        // TODO: OS module was extracted to its own service. Re-add the cross-check
        // against OtherService appointments via a REST call to the OS microservice.

        log.debug("Vehicle {} is available for rescheduling to {} {}",
                vehicleId, scheduleDate, serviceStartTime);
    }
}
