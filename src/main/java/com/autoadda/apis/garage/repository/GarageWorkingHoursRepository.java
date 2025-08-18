package com.autoadda.apis.garage.repository;
import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.GarageWorkingHours;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface GarageWorkingHoursRepository extends JpaRepository<GarageWorkingHours, Integer> {
	Optional<GarageWorkingHours> findByGarageIdAndDayOfWeek(Integer garageId, DayOfWeek dayOfWeek);



	@Query("SELECT g FROM GarageWorkingHours g WHERE g.garage.id = :garageId AND g.dayOfWeek = :dayOfWeek")
	Optional<GarageWorkingHours> findByGarageDetailsIdAndDayOfWeek(@Param("garageId") Integer garageId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

}
	