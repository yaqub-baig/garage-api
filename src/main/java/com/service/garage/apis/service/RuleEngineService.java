package com.service.garage.apis.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.MaintenanceScheduleRule;
import com.service.garage.apis.entity.VehicleDetails;
import com.service.garage.apis.repository.MaintenanceScheduleRuleRepository;
import com.service.garage.apis.util.IntervalType;

import jakarta.transaction.Transactional;

@Service
public class RuleEngineService {

	@Autowired
	private MaintenanceScheduleRuleRepository ruleRepo;

	/**
	 * Finds the applicable maintenance schedule rule for a given vehicle.
	 *
	 * @param vehicle The vehicle details for which to find the rule.
	 * @return An Optional containing the applicable rule if found, otherwise empty.
	 */
	@Transactional
	public Optional<MaintenanceScheduleRule> findApplicableRule(VehicleDetails vehicle) {
		return ruleRepo.findByMakeAndModelAndIsActiveTrue(vehicle.getMake(), vehicle.getModel());
	}

	@Transactional
	public LocalDate calculateNextServiceDate(VehicleDetails vehicle, MaintenanceScheduleRule rule) {
		if (rule.getIntervalType() == IntervalType.TIME) {
			return vehicle.getLastServiceDate().plusMonths(rule.getIntervalValue());
		} else {
			int mileageGap = rule.getIntervalValue();
			int avgKmPerDay = 40; // configurable
			int daysToNextService = mileageGap / avgKmPerDay;
			return vehicle.getLastServiceDate().plusDays(daysToNextService);
		}
	}

	/**
	 * Estimates the mileage of a vehicle at a given date based on its last service
	 * date and mileage.
	 *
	 * @param vehicle The vehicle details containing last service date and mileage.
	 * @param date    The date for which to estimate the mileage.
	 * @return Estimated mileage at the given date.
	 */
	@Transactional
	public Integer estimateMileageAtDate(VehicleDetails vehicle, LocalDate date) {
		int days = (int) ChronoUnit.DAYS.between(vehicle.getLastServiceDate(), date);
		int avgKmPerDay = 40; // configurable
		return vehicle.getLastServiceMileage() + (avgKmPerDay * days);
	}
}
