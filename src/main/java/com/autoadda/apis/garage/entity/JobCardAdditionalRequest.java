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
public class JobCardAdditionalRequest {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "status")
    private Status status;

    @Column(name = "technician_id")
    private Integer technicianId;

    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "garage_id")
    private Integer garageId;

    @Column(name = "request_type")
    private String requestType;

    @Column(name = "comments")
    private String comments;

    @Column(name = "insert_tms")
    private LocalDate insertTms;

}
