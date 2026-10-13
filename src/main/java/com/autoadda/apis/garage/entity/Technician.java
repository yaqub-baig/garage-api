package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.ProfileCompletionStatus;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "emirates_id")
    private String emiratesId;

    @Column(name = "auto_adda_id")
    private Integer autoAddaId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "technician_skill_set",
            joinColumns = @JoinColumn(name = "technician_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_set_id")
    )
    @Builder.Default
    private List<AvailableService> skillSet = new ArrayList<> ( );

    @OneToMany(mappedBy = "technician", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<TechnicianExpertise> expertise = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "technician_garage",
            joinColumns = @JoinColumn(name = "technician_id"),
            inverseJoinColumns = @JoinColumn(name = "garage_id")
    )
    @Builder.Default
    private Set<Garage> garages = new HashSet<>();

    @Column(name = "brand", columnDefinition = "LONGTEXT")
    private String brand;

    @Column(name = "model", columnDefinition = "LONGTEXT")
    private String model;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "year_of_experience")
    private Integer yearOfExperience;

    @Column(name = "city")
    private String city;

    @Column(name = "is_available_for_service")
    @Builder.Default
    private Boolean isAvailableForService = true;

    @Column(name = "profile_img")
    private String profileImg;

    @Enumerated(EnumType.STRING)
    private EMPLOYEE_ROLE employeeRole;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    // CV profile completion, recalculated by TechnicianProfileCompletionCalculator after each profile change
    @Column(name = "profile_completion_percent")
    @Builder.Default
    private Integer profileCompletionPercent = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_completion_status", length = 20)
    @Builder.Default
    private ProfileCompletionStatus profileCompletionStatus = ProfileCompletionStatus.NOT_STARTED;

    @Column(name = "profile_updated_at")
    private LocalDateTime profileUpdatedAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now ( );
        isDeleted = false;
    }
}
