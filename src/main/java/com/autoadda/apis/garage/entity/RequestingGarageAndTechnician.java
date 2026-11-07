package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.SendBy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestingGarageAndTechnician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne
    private Garage garage;
    @ManyToOne
    private Technician technician;
    @Enumerated(EnumType.STRING)
    private SendBy sendBy;
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;
    private LocalDateTime requestedAt;
    private String description;

    /** Role the person gets when the request is accepted (team invites). */
    @ManyToOne
    @JoinColumn(name = "role_id")
    private GarageRole role;

    /** Phone number the invite was sent to; set when the invitee had no AutoAdda account yet. */
    @Column(length = 30)
    private String invitedPhone;

    /** AutoAdda user id of the team member who sent the invite. */
    private Integer invitedBy;
    @PrePersist
    public void prePersist() {
        requestedAt = LocalDateTime.now();
    }

}
