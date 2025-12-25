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
    @PrePersist
    public void prePersist() {
        requestedAt = LocalDateTime.now();
    }

}
