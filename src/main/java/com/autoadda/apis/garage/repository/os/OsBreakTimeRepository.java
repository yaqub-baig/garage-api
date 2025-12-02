package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsBreakTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;


@Repository
public interface OsBreakTimeRepository extends JpaRepository<OsBreakTime, Integer> {

    List<OsBreakTime> findByOtherServiceIdAndDayOfWeek(Integer otherServiceId, DayOfWeek dayOfWeek);


}