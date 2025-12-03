package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageBreakTime;
import com.autoadda.apis.garage.entity.GarageWorkingHours;
import com.autoadda.apis.garage.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SlotGenerationService {
    private final SlotRepository slotRepository;
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final GarageWorkingHoursRepository workingHoursRepo;
    private final GarageBreakTimeRepository breakRepo;

    @PostConstruct
    public void init() {
        generateSlotsForNextDays ( );
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    public void generateSlotsForNextDays() {
        List<Garage> garages = garageRepository.findAll ( ).stream ( ).toList ( );
        LocalDate newDay = LocalDate.now ( ).plusDays ( 59 );
        garages.forEach ( g -> generateSlotsForGarageAndDate ( g, newDay ) );

    }


    public void generateSlotsForDays(Garage garage, int days) {
        LocalDate today = LocalDate.now ( );
        for (int i = 0; i < days; i++) {
            LocalDate targetDate = today.plusDays ( i );
            if ( garage.getConcurrentAppointmentCapacity ( ) != null )
                generateSlotsForGarageAndDate ( garage, targetDate );
        }
    }

    public void generateSlotsForGarageAndDate(Garage garage, LocalDate date) {
        DayOfWeek day = date.getDayOfWeek ( );
        GarageWorkingHours workingHours = workingHoursRepo.findByGarageIdAndDayOfWeek ( garage.getId ( ), day )
                .orElse ( null );
        List<GarageBreakTime> breaks = breakRepo.findByGarageIdAndDayOfWeek ( garage.getId ( ), day );

        if ( workingHours == null || workingHours.isClosed ( ) ) {
            return;
        }

        int maxBookings = garage.getConcurrentAppointmentCapacity ( ) != null ? garage.getConcurrentAppointmentCapacity ( ) : 0;
        int slotDuration = workingHours.getSlotDurationMinutes();

        LocalTime currentTime = workingHours.getOpenTime ( );
        LocalTime closeTime = workingHours.getCloseTime ( );

        while (currentTime.plusMinutes ( slotDuration ).compareTo ( closeTime ) <= 0) {
            LocalTime endTime = currentTime.plusMinutes ( slotDuration );

            LocalTime finalCurrentTime = currentTime;
            boolean inBreak = breaks.stream ( ).anyMatch ( b ->
                    (finalCurrentTime.isBefore ( b.getBreakEnd ( ) ) && endTime.isAfter ( b.getBreakStart ( ) ))
            );
            // Check if slot already exists
            if ( ! inBreak ) {
                Optional<AppointmentSlot> existingSlot = slotRepository.findByGarageIdAndSlotDateAndStartTime (
                        garage.getId ( ), date, currentTime );
                if ( existingSlot.isEmpty ( ) ) {
                    AppointmentSlot slot = new AppointmentSlot ( );
                    slot.setSlotDate ( date );
                    slot.setStartTime ( currentTime );
                    slot.setEndTime ( endTime );
                    slot.setMaxBookings ( maxBookings );
                    slot.setCurrentBookings ( 0 );
                    slot.setGarage ( garage );
                    slotRepository.save ( slot );
                }

            }
            currentTime = endTime;
        }
    }

    public void updateUpcomingSlotsForGarage(Garage garage) {
        if ( garage.getConcurrentAppointmentCapacity ( ) == null ) {
            return; // no capacity set, nothing to update
        }
        int newCapacity = garage.getConcurrentAppointmentCapacity ( );
        LocalDate today = LocalDate.now ( );

        // Fetch all future slots for this garage
        List<AppointmentSlot> upcomingSlots = slotRepository.findByGarageIdAndSlotDateGreaterThanEqual (
                garage.getId ( ), today
        );

        for (AppointmentSlot slot : upcomingSlots) {
            slot.setMaxBookings ( newCapacity );

            // ✅ Edge case: if current bookings > new capacity,
            // still allow the booked ones but prevent new bookings
            if ( slot.getCurrentBookings ( ) > newCapacity ) {
                slot.setMaxBookings ( slot.getCurrentBookings ( ) );
            }
        }
        slotRepository.saveAll ( upcomingSlots );
    }

}
