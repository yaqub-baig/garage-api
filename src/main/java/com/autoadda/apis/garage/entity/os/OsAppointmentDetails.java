package com.autoadda.apis.garage.entity.os;

import com.autoadda.apis.garage.entity.AppointmentSlot;
import com.autoadda.apis.garage.entity.OtherService;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OsAppointmentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "appointment_reference_id")
    private String appointmentReferenceId;

    @Column(name = "schedule_date")
    private LocalDate scheduleDate;

    @Column(name = "service_start_time")
    private LocalTime serviceStartTime;

    @Column(name = "service_end_time")
    private LocalTime serviceEndTime;

    @Column(name = "delivery_time")
    private LocalTime deliveryTime;

    @Column(name = "status")
    private Status status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insert_tms;

    @ManyToOne
    @JoinColumn(name = "other_service_id")
    private OtherService otherService;

    @Column(name = "selected_other_service")
    private String selectedOtherServiceIds;

    @Column(name = "os_subscription_plan_id")
    private String osSubscriptionPlanId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @ManyToOne
    private OsAppointmentSlot slot;

    @Column(name = "technician_id", nullable = true)
    private Integer technicianId;
}
