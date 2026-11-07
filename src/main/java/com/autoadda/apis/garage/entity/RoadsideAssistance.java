package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoadsideAssistance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer vehicleOwnerId;

    private String vehicleNumber;

    private String issueDescription;

    private String location;

    private LocalDateTime requestTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String assignedTechnician;

    private String contactNumber;

    private boolean isTowed;

    private String remarks;

    @ManyToOne
    @JoinColumn(name = "voice_note_id")
    private MediaFile voiceNote;

    private LocalDateTime completedAt;
}
