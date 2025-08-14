package com.autoadda.apis.garage.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.GarageBreakTime;

public interface GarageBreakTimeRepository extends JpaRepository<GarageBreakTime, Integer> {
	List<GarageBreakTime> findByGarageIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);
}