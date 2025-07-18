package com.autoadda.apis.garage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.MaintenanceScheduleRule;

public interface MaintenanceScheduleRuleRepository extends JpaRepository<MaintenanceScheduleRule, Integer> {
    Optional<MaintenanceScheduleRule> findByMakeAndModelAndIsActiveTrue(String make, String model);
}