package com.service.garage.apis.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.AppointmentDetails;
import com.service.garage.apis.entity.Customer;
import com.service.garage.apis.entity.Garage;
import com.service.garage.apis.entity.GarageBreakTime;
import com.service.garage.apis.entity.GarageHolidayOverride;
import com.service.garage.apis.entity.GarageWorkingHours;
import com.service.garage.apis.repository.AppointmentDetailsRepository;
import com.service.garage.apis.repository.CustomerRepository;
import com.service.garage.apis.repository.GarageBreakTimeRepository;
import com.service.garage.apis.repository.GarageHolidayOverrideRepository;
import com.service.garage.apis.repository.GarageRepository;
import com.service.garage.apis.repository.GarageWorkingHoursRepository;
import com.service.garage.apis.util.DateHelper;
import com.service.garage.apis.util.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {

    private final GarageWorkingHoursRepository workingHoursRepo;
    private final GarageBreakTimeRepository breakRepo;
    private final GarageHolidayOverrideRepository holidayRepo;
    private final AppointmentDetailsRepository bookingRepo;
    private final GarageRepository garageRepo;
    private final CustomerRepository customerRepo;

	/**
	 * Retrieves available time slots for a garage on a specific date.
	 *
	 * @param garageId The ID of the garage.
	 * @param date     The date for which to retrieve available slots.
	 * @return A list of available LocalTime slots.
	 */
	@Transactional
	public List<LocalTime> getAvailableSlots(Integer garageId, LocalDate date) {
		DayOfWeek day = date.getDayOfWeek();

		Optional<GarageHolidayOverride> overrideOpt = holidayRepo.findByGarageIdAndDate(garageId, date);
		LocalTime openTime, closeTime;
		int slotDuration;

		if (overrideOpt.isPresent()) {
			GarageHolidayOverride override = overrideOpt.get();
			if (override.isClosed())
				return Collections.emptyList();
			openTime = override.getOpenTime();
			closeTime = override.getCloseTime();
			slotDuration = 30;
		} else {
			GarageWorkingHours workingHours = workingHoursRepo.findByGarageIdAndDayOfWeek(garageId, day)
					.orElseThrow(() -> new RuntimeException("No working hours defined"));

			if (workingHours.isClosed())
				return Collections.emptyList();

			openTime = workingHours.getOpenTime();
			closeTime = workingHours.getCloseTime();
			slotDuration = workingHours.getSlotDurationMinutes();
		}

		List<GarageBreakTime> breaks = breakRepo.findByGarageIdAndDayOfWeek(garageId, day);
		Set<LocalTime> booked = bookingRepo.findByGarage_IdAndScheduleDate(garageId, date).stream()
				.filter(b -> b.getStatus() == Status.CONFIRMED).map(AppointmentDetails::getServiceStartTime)
				.collect(Collectors.toSet());

		List<LocalTime> slots = new ArrayList<>();
        for (LocalTime t = openTime; t.plusMinutes(slotDuration).compareTo(closeTime) <= 0; t = t.plusMinutes(slotDuration)) {
            LocalTime finalT = t;
			boolean inBreak = breaks.stream()
                    .anyMatch(b -> ! finalT.isBefore(b.getBreakStart()) && finalT.isBefore(b.getBreakEnd()));
			if (!booked.contains(t) && !inBreak) {
				slots.add(t);
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
	public AppointmentDetails bookSlot(Integer garageId, Integer userId, LocalDate date, LocalTime time) {
		Optional<AppointmentDetails> existing = bookingRepo.findLockedSlot(garageId, date, time);
		if (existing.isPresent() && existing.get().getStatus() == Status.CONFIRMED) {
			throw new IllegalStateException("Slot already booked.");
		}

//		if (bookingRepo.existsByCustomer_IdAndGarage_IdAndScheduleDateAndServiceStartTimeAndStatus(userId, garageId,
//				date, time, Status.CONFIRMED)) {
//			throw new IllegalStateException("User has already booked this slot.");
//		}

		Garage garage = garageRepo.findById(garageId).orElseThrow(() -> new RuntimeException("Garage not found"));
		Customer customer = customerRepo.findById(userId).orElseThrow(() -> new RuntimeException("Customer not found"));

		AppointmentDetails appointment = AppointmentDetails.builder().garage(garage).scheduleDate(date)
				.serviceStartTime(time).status(Status.CONFIRMED).comments("Booked via platform")
				.insert_tms(DateHelper.dbDate()).build();

		return bookingRepo.save(appointment);
	}

	/**
	 * Cancels a booking for a specific garage, date, and time.
	 *
	 * @param garageId The ID of the garage.
	 * @param date     The date of the booking.
	 * @param time     The time of the booking.
	 */
	@Transactional
	public void cancelBooking(Integer garageId, LocalDate date, LocalTime time) {
		AppointmentDetails booking = bookingRepo.findByGarage_IdAndScheduleDateAndServiceStartTime(garageId, date, time)
				.orElseThrow(() -> new RuntimeException("No such booking found to cancel."));
		booking.setStatus(Status.CANCELLED);
		bookingRepo.save(booking);
	}

	/**
	 * Retrieves all bookings made by a specific user.
	 *
	 * @param userId The ID of the user.
	 * @return A list of AppointmentDetails for the user's bookings.
	 */
//	@Transactional
//	public List<AppointmentDetails> getBookingsByUser(Integer userId) {
//		return bookingRepo.findByCustomer_Id(userId);
//	}
}
