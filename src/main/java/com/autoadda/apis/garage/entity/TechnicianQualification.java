package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.QualificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "technician_qualification", indexes = @Index(name = "idx_tech_qualification_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianQualification extends TechnicianProfileItem {

    @Enumerated(EnumType.STRING)
    @Column(name = "qualification_type", length = 30)
    private QualificationType qualificationType;

    @Column(name = "title")
    private String title;

    @Column(name = "field_of_study")
    private String fieldOfStudy;

    @Column(name = "institution")
    private String institution;

    @Column(name = "country")
    private String country;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private Boolean isCurrent;

    @Column(name = "grade")
    private String grade;

    @OneToOne
    @JoinColumn(name = "certificate_file_id")
    private MediaFile certificateFile;
}
