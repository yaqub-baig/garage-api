package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.LanguageProficiency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "technician_language", indexes = @Index(name = "idx_tech_language_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianLanguage extends TechnicianProfileItem {

    @Column(name = "language")
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", length = 30)
    private LanguageProficiency proficiency;
}
