package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.entity.os.OsAvailableService;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "other_service_bridge")
@Data
@Getter
@Setter
public class OtherServiceBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "other_garage_id")
    @JsonBackReference("service-bridges")
    private OtherService otherServiceGarage;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private OsAvailableService availableService;

}
