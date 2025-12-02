package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.TechnicianShift;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.RevenueSummaryDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GarageAnalyticsService {

    private final JobCardRepository jobCardRepo;
    private final InvoiceRepository invoiceRepo;
    private final TechnicianRepository technicianRepo;
    private final TechnicianAvailabilityRepository availabilityRepo;
    private final TechnicianShiftRepository shiftRepository;


    /**
     * Retrieves the revenue summary for the last 7 days.
     *
     * @return RevenueSummaryDTO containing the revenue and job count for the last
     * week
     */
    @Transactional
    public RevenueSummaryDTO getWeeklySummary() {
        LocalDateTime now = LocalDateTime.now ( );
        LocalDateTime oneWeekAgo = now.minusWeeks ( 1 );

        double revenue = Optional.ofNullable ( invoiceRepo.calculateTotalRevenue ( oneWeekAgo, now ) ).orElse ( 0.0 );
        long totalJobs = jobCardRepo.countCompletedJobsBetween ( oneWeekAgo, now );

        return new RevenueSummaryDTO ( oneWeekAgo.toLocalDate ( ), now.toLocalDate ( ), revenue, totalJobs );
    }


    private double calculateUtilization(long jobsHandled) {
        int expectedJobs = 50; // you can configure per technician per period
        return (double) jobsHandled / expectedJobs * 100;
    }

    /**
     * Calculates true technician utilization based on their shift schedule and
     * number of jobs completed in the given time window.
     *
     * @param technicianId  Technician's ID
     * @param jobsCompleted Number of jobs completed in the timeframe
     * @param from          Start of the period
     * @param to            End of the period
     * @return Utilization percentage (0 to 100)
     */
    @Transactional
    public double calculateTrueUtilization(Integer technicianId, long jobsCompleted, LocalDateTime from,
                                           LocalDateTime to) {
        List<TechnicianShift> shifts = shiftRepository.findShiftsInPeriod ( technicianId, from.toLocalDate ( ),
                to.toLocalDate ( ) );

        long totalWorkingMinutes = 0;

        for (TechnicianShift shift : shifts) {
            long shiftMinutes = Duration.between ( shift.getStartTime ( ), shift.getEndTime ( ) ).toMinutes ( );

            if ( shift.getSpecificDate ( ) != null ) {
                // One-time shift on a specific date
                LocalDate date = shift.getSpecificDate ( );
                if ( ! date.isBefore ( from.toLocalDate ( ) ) && ! date.isAfter ( to.toLocalDate ( ) ) ) {
                    totalWorkingMinutes += shiftMinutes;
                }
            } else if ( shift.isActive ( ) ) {
                // Recurring weekly shift
                long matchingDays = from.toLocalDate ( ).datesUntil ( to.toLocalDate ( ).plusDays ( 1 ) )
                        .filter ( date -> date.getDayOfWeek ( ) == shift.getDayOfWeek ( ) ).count ( );
                totalWorkingMinutes += matchingDays * shiftMinutes;
            }
        }

        long estimatedTimePerJobInMinutes = 60; // You may make this configurable
        long totalJobTime = jobsCompleted * estimatedTimePerJobInMinutes;

        if ( totalWorkingMinutes == 0 ) {
            return 0.0; // Avoid division by zero
        }

        return (double) totalJobTime / totalWorkingMinutes * 100;
    }
}
