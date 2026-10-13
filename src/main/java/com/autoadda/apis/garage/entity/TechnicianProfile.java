package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Gender;
import com.autoadda.apis.garage.util.MaritalStatus;
import com.autoadda.apis.garage.util.VisaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Extended personal details and emergency contact of a technician (CV profile).
 * One row per technician, created lazily on the first save of the personal or references step.
 * Every column is optional.
 */
@Entity
@Table(name = "technician_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnicianProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false, unique = true)
    private Technician technician;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 30)
    private Gender gender;

    @Column(name = "nationality")
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 30)
    private MaritalStatus maritalStatus;

    @Column(name = "alternate_phone")
    private String alternatePhone;

    @Column(name = "address_line", length = 500)
    private String addressLine;

    @Column(name = "country")
    private String country;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "visa_status", length = 30)
    private VisaStatus visaStatus;

    @Column(name = "visa_expiry")
    private LocalDate visaExpiry;

    @Column(name = "driving_license_number")
    private String drivingLicenseNumber;

    @Column(name = "professional_summary", columnDefinition = "TEXT")
    private String professionalSummary;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_relation")
    private String emergencyContactRelation;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
