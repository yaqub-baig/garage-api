package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.TeamMemberStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A person's membership of one garage's team, with the role they hold there.
 */
@Entity
@Table(name = "garage_team_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_garage_team_member", columnNames = {"garage_id", "technician_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private GarageRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeamMemberStatus status = TeamMemberStatus.ACTIVE;

    /** AutoAdda user id of whoever added this member (null for backfilled / owner rows). */
    private Integer invitedBy;

    private LocalDateTime joinedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
