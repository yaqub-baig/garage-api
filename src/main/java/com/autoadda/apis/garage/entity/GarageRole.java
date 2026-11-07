package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.GaragePermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A role defined by a garage (e.g. Admin, HR, Front Desk) with the permissions it grants.
 */
@Entity
// Not "garage_role": older databases still contain a legacy garage_role table (role_code / description)
// from an entity removed long ago, and ddl-auto=update cannot reshape it.
@Table(name = "garage_team_role",
        uniqueConstraints = @UniqueConstraint(name = "uk_garage_team_role_name", columnNames = {"garage_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garage_id", nullable = false)
    @JsonIgnore
    private Garage garage;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 255)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "garage_team_role_permission", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission", length = 40)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<GaragePermission> permissions = new HashSet<>();

    /** System roles (Owner) cannot be edited or deleted. */
    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
