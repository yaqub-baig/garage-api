package com.autoadda.apis.garage.entity;

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
public class AppointmentDetails {

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
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insert_tms;

    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @Column(name = "selected_garage_service")
    private String selectedGarageServiceIds;

    @Column(name = "garage_subscription_plan_id")
    private String garageSubscriptionPlanId;

    @Column(name = "customer_id", nullable = false)
    private Integer customerId;

    @Column(name = "vehicle_id")
    private Integer vehicleId;

    @ManyToOne
    private AppointmentSlot slot;

    @Column(name = "technician_id", nullable = true)
    private Integer technicianId;
}
