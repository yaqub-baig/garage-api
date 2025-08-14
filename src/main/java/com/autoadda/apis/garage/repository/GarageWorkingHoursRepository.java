package com.autoadda.apis.garage.repository;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.GarageWorkingHours;

public interface GarageWorkingHoursRepository extends JpaRepository<GarageWorkingHours, Integer> {
	Optional<GarageWorkingHours> findByGarageIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);
}
	