package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.TechnicianEmployment;
import com.autoadda.apis.garage.response.EmploymentGapResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Finds periods in a technician's work history that are covered by neither a job nor a career break.
 * <p>
 * Each entry with a start date covers [startDate, endDate]; a current entry runs until today and an entry
 * without an end date (and not current) covers only its start date. Overlapping or adjacent entries are merged.
 * Gaps between entries, and the gap from the last entry until today when nothing is current, are reported
 * when they last at least the configured number of months. Gaps before the first entry are not reported.
 */
@Component
public class EmploymentGapDetector {

    private final int thresholdMonths;

    public EmploymentGapDetector(@Value("${technician.profile.employment-gap-threshold-months:6}") int thresholdMonths) {
        this.thresholdMonths = thresholdMonths;
    }

    public int getThresholdMonths() {
        return thresholdMonths;
    }

    public List<EmploymentGapResponse> findUnexplainedGaps(List<TechnicianEmployment> entries) {
        return findUnexplainedGaps(entries, LocalDate.now());
    }

    List<EmploymentGapResponse> findUnexplainedGaps(List<TechnicianEmployment> entries, LocalDate today) {
        List<LocalDate[]> periods = entries.stream()
                .filter(e -> e.getStartDate() != null)
                .map(e -> new LocalDate[]{e.getStartDate(), coveredUntil(e, today)})
                .sorted(Comparator.comparing((LocalDate[] p) -> p[0]))
                .toList();

        List<EmploymentGapResponse> gaps = new ArrayList<>();
        if (periods.isEmpty()) {
            return gaps;
        }

        LocalDate coveredUntil = periods.get(0)[1];
        for (LocalDate[] period : periods.subList(1, periods.size())) {
            if (period[0].isAfter(coveredUntil.plusDays(1))) {
                addIfLongEnough(gaps, coveredUntil.plusDays(1), period[0].minusDays(1), false);
            }
            if (period[1].isAfter(coveredUntil)) {
                coveredUntil = period[1];
            }
        }

        if (coveredUntil.isBefore(today)) {
            addIfLongEnough(gaps, coveredUntil.plusDays(1), today, true);
        }
        return gaps;
    }

    private static LocalDate coveredUntil(TechnicianEmployment entry, LocalDate today) {
        LocalDate start = entry.getStartDate();
        LocalDate end;
        if (Boolean.TRUE.equals(entry.getIsCurrent())) {
            end = today;
        } else {
            end = entry.getEndDate() != null ? entry.getEndDate() : start;
        }
        return end.isBefore(start) ? start : end;
    }

    private void addIfLongEnough(List<EmploymentGapResponse> gaps, LocalDate start, LocalDate end, boolean ongoing) {
        long months = ChronoUnit.MONTHS.between(start, end.plusDays(1));
        if (months >= thresholdMonths) {
            gaps.add(EmploymentGapResponse.builder()
                    .startDate(start)
                    .endDate(end)
                    .months((int) months)
                    .ongoing(ongoing)
                    .build());
        }
    }
}
