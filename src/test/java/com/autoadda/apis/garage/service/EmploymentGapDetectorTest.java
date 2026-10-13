package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.TechnicianEmployment;
import com.autoadda.apis.garage.response.EmploymentGapResponse;
import com.autoadda.apis.garage.util.EmploymentEntryType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmploymentGapDetectorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 13);

    private final EmploymentGapDetector detector = new EmploymentGapDetector(6);

    @Test
    void yearBetweenJobs_isReported() {
        List<EmploymentGapResponse> gaps = detector.findUnexplainedGaps(List.of(
                job("2015-01-01", "2019-12-31"),
                current("2021-01-01")), TODAY);

        assertEquals(1, gaps.size());
        assertEquals(LocalDate.of(2020, 1, 1), gaps.get(0).getStartDate());
        assertEquals(LocalDate.of(2020, 12, 31), gaps.get(0).getEndDate());
        assertEquals(12, gaps.get(0).getMonths());
        assertFalse(gaps.get(0).getOngoing());
    }

    @Test
    void careerBreakCoveringGap_explainsIt() {
        List<EmploymentGapResponse> gaps = detector.findUnexplainedGaps(List.of(
                job("2015-01-01", "2019-12-31"),
                careerBreak("2020-01-01", "2020-12-31"),
                current("2021-01-01")), TODAY);

        assertTrue(gaps.isEmpty());
    }

    @Test
    void careerBreakCoveringPartOfGap_leavesShorterGap() {
        List<EmploymentGapResponse> gaps = detector.findUnexplainedGaps(List.of(
                job("2015-01-01", "2018-12-31"),
                careerBreak("2019-01-01", "2019-03-31"),
                current("2020-01-01")), TODAY);

        assertEquals(1, gaps.size());
        assertEquals(LocalDate.of(2019, 4, 1), gaps.get(0).getStartDate());
        assertEquals(9, gaps.get(0).getMonths());
    }

    @Test
    void gapShorterThanThreshold_isIgnored() {
        List<EmploymentGapResponse> gaps = detector.findUnexplainedGaps(List.of(
                job("2015-01-01", "2019-12-31"),
                current("2020-04-01")), TODAY);

        assertTrue(gaps.isEmpty());
    }

    @Test
    void overlappingJobs_andUnsortedInput_areMerged() {
        List<EmploymentGapResponse> gaps = detector.findUnexplainedGaps(List.of(
                current("2018-01-01"),
                job("2015-01-01", "2017-06-30"),
                job("2017-01-01", "2017-12-31")), TODAY);

        assertTrue(gaps.isEmpty());
    }

    @Test
    void noCurrentJob_reportsOngoingGapUntilToday() {
        List<EmploymentGapResponse> gaps = detector.findUnexplainedGaps(List.of(
                job("2015-01-01", "2025-06-30")), TODAY);

        assertEquals(1, gaps.size());
        assertTrue(gaps.get(0).getOngoing());
        assertEquals(LocalDate.of(2025, 7, 1), gaps.get(0).getStartDate());
        assertEquals(TODAY, gaps.get(0).getEndDate());
    }

    @Test
    void entriesWithoutStartDate_andEmptyList_produceNoGaps() {
        TechnicianEmployment undated = new TechnicianEmployment();
        undated.setEmployerName("Unknown dates");

        assertTrue(detector.findUnexplainedGaps(List.of(undated), TODAY).isEmpty());
        assertTrue(detector.findUnexplainedGaps(List.of(), TODAY).isEmpty());
    }

    private static TechnicianEmployment job(String start, String end) {
        TechnicianEmployment e = new TechnicianEmployment();
        e.setStartDate(LocalDate.parse(start));
        e.setEndDate(LocalDate.parse(end));
        return e;
    }

    private static TechnicianEmployment current(String start) {
        TechnicianEmployment e = new TechnicianEmployment();
        e.setStartDate(LocalDate.parse(start));
        e.setIsCurrent(true);
        return e;
    }

    private static TechnicianEmployment careerBreak(String start, String end) {
        TechnicianEmployment e = job(start, end);
        e.setEntryType(EmploymentEntryType.CAREER_BREAK);
        return e;
    }
}
