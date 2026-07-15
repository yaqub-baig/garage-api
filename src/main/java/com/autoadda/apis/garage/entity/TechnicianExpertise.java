package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.ExpertiseLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a technician's expertise in a specific category with a proficiency level.
 * A technician can have multiple expertise areas, each with an associated level.
 */
@Entity
@Table(
    name = "technician_expertise",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_technician_category",
        columnNames = {"technician_id", "expertise_category_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicianExpertise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expertise_category_id", nullable = false)
    private ExpertiseCategory expertiseCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "expertise_level", nullable = false, length = 10)
    private ExpertiseLevel expertiseLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Set timestamps on entity creation
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Update timestamp on entity modification
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
