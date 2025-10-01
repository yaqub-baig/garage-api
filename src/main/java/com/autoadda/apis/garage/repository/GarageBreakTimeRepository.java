package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageBreakTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;


@Repository
public interface GarageBreakTimeRepository extends JpaRepository<GarageBreakTime, Integer> {
//    List<GarageBreakTime> findByGarageDetailsIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);


    List<GarageBreakTime> findByGarageIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);


}