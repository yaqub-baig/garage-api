package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.CustomerChoice;
import com.autoadda.apis.garage.util.PartCondition;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerPartResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "part_request_id", nullable = false)
    private PartRequest partRequest;

    @Column(name = "choice", nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerChoice choice;

    @Column(name = "part_condition")
    @Enumerated(EnumType.STRING)
    private PartCondition partCondition;

    @Column(name = "response_date", nullable = false)
    private LocalDateTime responseDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "customer_comments")
    private String customerComments;

    @PrePersist
    protected void onCreate() {
        if (responseDate == null) {
            responseDate = LocalDateTime.now();
        }
    }
}
