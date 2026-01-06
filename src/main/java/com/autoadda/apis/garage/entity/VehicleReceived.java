package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.VehicleRecievedStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleReceived {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private AppointmentDetails appointment;

    @ManyToOne
    private Technician technician;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_received_id")
    private List<MediaFile> vehiclePhotos = new ArrayList<> ( );

    @Enumerated(EnumType.STRING)
    private VehicleRecievedStatus status;
    private String comment;

    private Boolean isMainKeyReceived;
    private Boolean isSpareKeyReceived;
    private Boolean isToolKitReceived;
    private Boolean isJackReceived;
    private Boolean isWheelSpannerReceived;
    private Boolean isSpareTyreReceived;
    private Boolean isFireExtinguisherReceived;
    private Boolean isFirstAidKitReceived;
    private Boolean isCentralLockRemoteReceived;
    private Boolean isRCBookReceived;
    private Boolean isInsuranceCopyReceived;
    private Boolean isPUCReceived;

    private LocalDateTime createdAt;


    @PrePersist
    protected void onCreate() {
        status = VehicleRecievedStatus.RECEIVED;
        createdAt = LocalDateTime.now ( );
    }
}
