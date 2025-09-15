package com.autoadda.apis.garage.repository.os;

import java.time.LocalDate;
import java.util.Optional;

import com.autoadda.apis.garage.entity.os.OsHolidayOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.GarageHolidayOverride;

public interface OsHolidayOverrideRepository extends JpaRepository<OsHolidayOverride, Integer> {
    Optional<OsHolidayOverride> findByOtherServiceIdAndDate(Integer otherServiceId, LocalDate date);
}