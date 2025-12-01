package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppointmentStatusActivity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "appointment_id")
    private Integer appointmentId;

    @Column(name = "status")
    private Status status;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insertTms;

    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "member_type")
    private String memberType;

}
