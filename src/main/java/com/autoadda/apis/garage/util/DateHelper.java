package com.autoadda.apis.garage.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateHelper {

	private static final ZoneId UAE_ZONE = ZoneId.of("Asia/Dubai");

	// Returns current date in UAE timezone
	public static LocalDate dbDate() {
		return ZonedDateTime.now(UAE_ZONE).toLocalDate();
	}

	// Returns tomorrow's date in UAE timezone
	public static LocalDate getVerifyEndDate() {
		return ZonedDateTime.now(UAE_ZONE).plusDays(1).toLocalDate();
	}

	// Returns date after 90 days in UAE timezone
	public static LocalDate getDateWith90Days() {
		return ZonedDateTime.now(UAE_ZONE).plusDays(90).toLocalDate();
	}

	// --- Time Methods ---

	// Returns current time in UAE timezone
	public static LocalTime dbTime() {
		return ZonedDateTime.now(UAE_ZONE).toLocalTime();
	}

	// Returns time in UAE timezone after adding given minutes
	public static LocalTime getTimeAfterMinutes(int minutes) {
		return ZonedDateTime.now(UAE_ZONE).plusMinutes(minutes).toLocalTime();
	}

	// Returns time in UAE timezone after adding given hours
	public static LocalTime getTimeAfterHours(int hours) {
		return ZonedDateTime.now(UAE_ZONE).plusHours(hours).toLocalTime();
	}
}
