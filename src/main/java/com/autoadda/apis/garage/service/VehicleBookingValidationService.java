package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.entity.os.OsAppointmentDetails;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.os.OsAppointmentDetailsRepository;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Service responsible for validating vehicle booking constraints.
 * Prevents duplicate bookings across all garages and other services.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleBookingValidationService {

    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final OsAppointmentDetailsRepository osAppointmentDetailsRepository;

    /**
     * Validates that a vehicle is not already booked at the specified date/time.
     * Checks across both Garage appointments and OtherService appointments.
     *
     * @param vehicleId        Vehicle ID to check
     * @param scheduleDate     Date of the appointment
     * @param serviceStartTime Start time of the service
     * @throws ConflictException if vehicle already has a confirmed booking at this time
     */
    public void validateVehicleAvailability(Integer vehicleId, LocalDate scheduleDate, LocalTime serviceStartTime) {
        log.debug("Validating vehicle availability: vehicleId={}, date={}, time={}",
                vehicleId, scheduleDate, serviceStartTime);

        // Check in Garage appointments
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
        //todo: add rest client api for validate if car has otherservice booking
        // Check in OtherService appointments
        boolean hasOsBooking = osAppointmentDetailsRepository
                .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                        vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED);

        if (hasOsBooking) {
            Optional<OsAppointmentDetails> conflictingBooking = osAppointmentDetailsRepository
                    .findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED);

            String serviceName = conflictingBooking
                    .map(booking -> booking.getOtherService().getName())
                    .orElse("Unknown Service");

            log.warn("Duplicate booking attempt: Vehicle {} already booked at {} {} in other service {}",
                    vehicleId, scheduleDate, serviceStartTime, serviceName);

            throw new ConflictException(
                    String.format("Vehicle is already booked at %s on %s at service '%s'. " +
                                    "A vehicle cannot be at multiple locations at the same time.",
                            serviceStartTime, scheduleDate, serviceName)
            );
        }

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
     * @param isGarageAppointment  true if editing Garage appointment, false for OtherService
     * @throws ConflictException if vehicle has conflicting booking
     */
    public void validateVehicleAvailabilityForEdit(
            Integer vehicleId,
            LocalDate scheduleDate,
            LocalTime serviceStartTime,
            Integer currentAppointmentId,
            boolean isGarageAppointment) {

        log.debug("Validating vehicle availability for edit: vehicleId={}, date={}, time={}, " +
                        "currentAppointmentId={}, isGarageAppointment={}",
                vehicleId, scheduleDate, serviceStartTime, currentAppointmentId, isGarageAppointment);

        // Check in Garage appointments
        boolean hasGarageConflict;
        if (isGarageAppointment) {
            // Editing garage appointment - exclude current appointment from check
            hasGarageConflict = appointmentDetailsRepository
                    .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatusAndIdNot(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED, currentAppointmentId);
        } else {
            // Editing OS appointment - check all garage appointments
            hasGarageConflict = appointmentDetailsRepository
                    .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED);
        }

        if (hasGarageConflict) {
            Optional<AppointmentDetails> conflictingBooking = appointmentDetailsRepository
                    .findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED)
                    .filter(booking -> !isGarageAppointment || !booking.getId().equals(currentAppointmentId));

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

        //todo: add rest client api for validate if car has otherservice booking
        // Check in OtherService appointments
        boolean hasOsConflict;
        if (!isGarageAppointment) {
            // Editing OS appointment - exclude current appointment
            hasOsConflict = osAppointmentDetailsRepository
                    .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatusAndIdNot(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED, currentAppointmentId);
        } else {
            // Editing garage appointment - check all OS appointments
            hasOsConflict = osAppointmentDetailsRepository
                    .existsByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED);
        }
        if (hasOsConflict) {
            Optional<OsAppointmentDetails> conflictingBooking = osAppointmentDetailsRepository
                    .findByVehicleIdAndScheduleDateAndServiceStartTimeAndStatus(
                            vehicleId, scheduleDate, serviceStartTime, Status.CONFIRMED)
                    .filter(booking -> isGarageAppointment || !booking.getId().equals(currentAppointmentId));

            String serviceName = conflictingBooking
                    .map(booking -> booking.getOtherService().getName())
                    .orElse("Unknown Service");

            log.warn("Edit conflict: Vehicle {} already booked at {} {} in other service {}",
                    vehicleId, scheduleDate, serviceStartTime, serviceName);

            throw new ConflictException(
                    String.format("Cannot reschedule: Vehicle is already booked at %s on %s at service '%s'.",
                            serviceStartTime, scheduleDate, serviceName)
            );
        }

        log.debug("Vehicle {} is available for rescheduling to {} {}",
                vehicleId, scheduleDate, serviceStartTime);
    }
}
