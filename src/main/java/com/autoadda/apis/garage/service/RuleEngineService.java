package com.autoadda.apis.garage.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.MaintenanceScheduleRule;
import com.autoadda.apis.garage.entity.VehicleDetails;
import com.autoadda.apis.garage.repository.MaintenanceScheduleRuleRepository;
import com.autoadda.apis.garage.util.IntervalType;

@Service
public class RuleEngineService {

    @Autowired
    private MaintenanceScheduleRuleRepository ruleRepo;

    public Optional<MaintenanceScheduleRule> findApplicableRule(VehicleDetails vehicle) {
        return ruleRepo.findByMakeAndModelAndIsActiveTrue(vehicle.getMake(), vehicle.getModel());
    }

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

    public Integer estimateMileageAtDate(VehicleDetails vehicle, LocalDate date) {
        int days = (int) ChronoUnit.DAYS.between(vehicle.getLastServiceDate(), date);
        int avgKmPerDay = 40; // configurable
        return vehicle.getLastServiceMileage() + (avgKmPerDay * days);
    }
}
