package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.CareerBreakReason;
import com.autoadda.apis.garage.util.EmploymentEntryType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Work history entry on the technician CV: a past/current job, or a career break explaining a gap.
 * Not to be confused with job-card work history (TechnicianWorkHistoryResponse).
 */
@Entity
@Table(name = "technician_employment", indexes = @Index(name = "idx_tech_employment_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianEmployment extends TechnicianProfileItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 20)
    private EmploymentEntryType entryType = EmploymentEntryType.EMPLOYMENT;

    // Career break only: why the technician was not working during this period
    @Enumerated(EnumType.STRING)
    @Column(name = "break_reason", length = 30)
    private CareerBreakReason breakReason;

    @Column(name = "break_description", columnDefinition = "TEXT")
    private String breakDescription;

    @Column(name = "employer_name")
    private String employerName;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "reason_for_leaving", length = 1000)
    private String reasonForLeaving;
}
