package com.autoadda.apis.garage.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.TechnicianShift;
import com.autoadda.apis.garage.repository.InvoiceRepository;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.repository.TechnicianAvailabilityRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.repository.TechnicianShiftRepository;
import com.autoadda.apis.garage.request.GarageAnalyticsDTO;
import com.autoadda.apis.garage.request.RevenueSummaryDTO;
import com.autoadda.apis.garage.request.TechnicianSummaryDTO;
import com.autoadda.apis.garage.request.TechnicianUtilizationDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GarageAnalyticsService {

	private final JobCardRepository jobCardRepo;
	private final InvoiceRepository invoiceRepo;
	private final TechnicianRepository technicianRepo;
	private final TechnicianAvailabilityRepository availabilityRepo;
	private final TechnicianShiftRepository shiftRepository;

	/**
	 * Retrieves today's analytics for a specific garage, including job count,
	 * revenue, and technician utilization.
	 *
	 * @param garageId ID of the garage
	 * @return GarageAnalyticsDTO containing today's analytics
	 */
//	@Transactional
//	public GarageAnalyticsDTO getTodayAnalytics(Integer garageId) {
//		LocalDate today = LocalDate.now();
//		LocalDateTime start = today.atStartOfDay();
//		LocalDateTime end = today.atTime(LocalTime.MAX);
//
//		long jobCount = jobCardRepo.countJobsByDay(start, end, garageId);
//		BigDecimal revenue = invoiceRepo.getRevenueByDay(start, end, garageId);
//		if (revenue == null)
//			revenue = BigDecimal.ZERO;
//
//		List<Object[]> completed = jobCardRepo.countCompletedJobsPerTechnician(garageId);
//		List<Object[]> available = availabilityRepo.getAvailableHoursPerTechnician(garageId);
//
//		Map<Long, Integer> completedMap = completed.stream()
//				.collect(Collectors.toMap(obj -> (Long) obj[0], obj -> ((Long) obj[1]).intValue()));
//
//		Map<Long, Integer> availableMap = available.stream()
//				.collect(Collectors.toMap(obj -> (Long) obj[0], obj -> ((Long) obj[1]).intValue()));
//
//		List<Technician> techs = technicianRepo.findByGarageId(garageId);
//		List<TechnicianUtilizationDTO> utilizations = new ArrayList<>();
//
//		for (Technician tech : techs) {
//			int done = completedMap.getOrDefault(tech.getId(), 0);
//			int availableHours = availableMap.getOrDefault(tech.getId(), 8); // default 8 hours
//			double percent = availableHours == 0 ? 0.0 : (done * 1.0 / availableHours) * 100.0;
//
//			TechnicianUtilizationDTO dto = new TechnicianUtilizationDTO();
//			dto.setTechnicianId(tech.getId());
//			dto.setTechnicianName(tech.getFirstName());
//			dto.setJobsCompleted(done);
//			dto.setAvailableHours(availableHours);
//			dto.setUtilizationPercent(percent);
//
//			utilizations.add(dto);
//		}
//
//		GarageAnalyticsDTO analytics = new GarageAnalyticsDTO();
//		analytics.setDate(today);
//		analytics.setJobsToday(jobCount);
//		analytics.setRevenueToday(revenue);
//		analytics.setTechnicianUtilizations(utilizations);
//
//		return analytics;
//	}

	/**
	 * Retrieves the revenue summary for the last 7 days.
	 *
	 * @return RevenueSummaryDTO containing the revenue and job count for the last
	 *         week
	 */
	@Transactional
	public RevenueSummaryDTO getWeeklySummary() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime oneWeekAgo = now.minusWeeks(1);

		double revenue = Optional.ofNullable(invoiceRepo.calculateTotalRevenue(oneWeekAgo, now)).orElse(0.0);
		long totalJobs = jobCardRepo.countCompletedJobsBetween(oneWeekAgo, now);

		return new RevenueSummaryDTO(oneWeekAgo.toLocalDate(), now.toLocalDate(), revenue, totalJobs);
	}

	/**
	 * Retrieves a breakdown of technician performance, including total jobs handled
	 * and revenue generated in the specified time period.
	 *
	 * @param from Start of the period
	 * @param to   End of the period
	 * @return List of TechnicianSummaryDTO containing performance metrics
	 */
	@Transactional
//	public List<TechnicianSummaryDTO> getTechnicianBreakdown(LocalDateTime from, LocalDateTime to) {
//		List<Object[]> results = jobCardRepo.findTechnicianSummary(from, to);
//		List<TechnicianSummaryDTO> summaries = new ArrayList<>();
//
//		for (Object[] row : results) {
//			TechnicianSummaryDTO dto = new TechnicianSummaryDTO();
//			dto.setTechnicianName((String) row[0]);
//			dto.setTotalJobs((Long) row[1]);
//			dto.setTotalRevenue((Double) row[2]);
//			dto.setUtilizationPercentage(calculateUtilization((Long) row[1])); // Stub
//			summaries.add(dto);
//		}
//
//		return summaries;
//	}

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
		List<TechnicianShift> shifts = shiftRepository.findShiftsInPeriod(technicianId, from.toLocalDate(),
				to.toLocalDate());

		long totalWorkingMinutes = 0;

		for (TechnicianShift shift : shifts) {
			long shiftMinutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();

			if (shift.getSpecificDate() != null) {
				// One-time shift on a specific date
				LocalDate date = shift.getSpecificDate();
				if (!date.isBefore(from.toLocalDate()) && !date.isAfter(to.toLocalDate())) {
					totalWorkingMinutes += shiftMinutes;
				}
			} else if (shift.isActive()) {
				// Recurring weekly shift
				long matchingDays = from.toLocalDate().datesUntil(to.toLocalDate().plusDays(1))
						.filter(date -> date.getDayOfWeek() == shift.getDayOfWeek()).count();
				totalWorkingMinutes += matchingDays * shiftMinutes;
			}
		}

		long estimatedTimePerJobInMinutes = 60; // You may make this configurable
		long totalJobTime = jobsCompleted * estimatedTimePerJobInMinutes;

		if (totalWorkingMinutes == 0) {
			return 0.0; // Avoid division by zero
		}

		return (double) totalJobTime / totalWorkingMinutes * 100;
	}
}
