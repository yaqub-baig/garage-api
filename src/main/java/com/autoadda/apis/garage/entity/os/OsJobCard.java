package com.autoadda.apis.garage.entity.os;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OsJobCard {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "service_start_time")
    private LocalTime serviceStartTime;

    @Column(name = "service_end_time")
    private LocalTime serviceEndTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne
    @JoinColumn(name = "other_service_id", referencedColumnName = "id")
    private OtherService otherService;

    @ManyToOne
    @JoinColumn(name = "technician_id", referencedColumnName = "id")
    private Technician technician;

//    @ManyToOne
//    @JoinColumn(name = "os_appointment_details_id", referencedColumnName = "id")
    private Integer osAppointmentId;

    @ManyToOne
    @JoinColumn(name = "appointment_service_id", referencedColumnName = "id")
    private OsAppointmentServices service;

    @ManyToOne
    @JoinColumn(name = "work_order_id")
    private WorkOrder workOrder;

    @Column(name = "comments")
    private String comments;

}
