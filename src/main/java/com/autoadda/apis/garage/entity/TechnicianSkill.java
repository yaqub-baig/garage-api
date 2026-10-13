package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.ExpertiseLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Free-text technical skill on the technician CV (e.g. "ECU diagnostics", "Bosch KTS").
 * Complements the catalogue-based skillSet and expertise on {@link Technician}.
 */
@Entity
@Table(name = "technician_skill", indexes = @Index(name = "idx_tech_skill_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianSkill extends TechnicianProfileItem {

    @Column(name = "skill_name")
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 10)
    private ExpertiseLevel level;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
}
