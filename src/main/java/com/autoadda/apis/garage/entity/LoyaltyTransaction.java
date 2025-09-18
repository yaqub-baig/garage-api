package com.autoadda.apis.garage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int pointsChanged;
    private String description;

    private LocalDateTime transactionDate;

    @ManyToOne
    private VehicleOwner owner;

    @ManyToOne
    private Garage garage;
}
