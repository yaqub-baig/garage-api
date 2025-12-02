package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsHolidayOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface OsHolidayOverrideRepository extends JpaRepository<OsHolidayOverride, Integer> {
    Optional<OsHolidayOverride> findByOtherServiceIdAndDate(Integer otherServiceId, LocalDate date);
}