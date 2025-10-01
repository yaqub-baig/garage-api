package com.autoadda.apis.garage.repository.os;
import java.time.DayOfWeek;
import java.util.Optional;

import com.autoadda.apis.garage.entity.os.OsWorkHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface OsWorkHourRepository extends JpaRepository<OsWorkHours, Integer> {
    Optional<OsWorkHours> findByOtherServiceIdAndDayOfWeek(Integer otherServiceId, DayOfWeek dayOfWeek);



    @Query("SELECT g FROM OsWorkHours g WHERE g.otherService.id = :otherServiceId AND g.dayOfWeek = :dayOfWeek")
    Optional<OsWorkHours> findByOtherServiceDetailsIdAndDayOfWeek(@Param("otherServiceId") Integer garageId, @Param("dayOfWeek") DayOfWeek dayOfWeek);

}
