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
public class LoyaltyPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private int points;
    private int redeemedPoints;

    private LocalDateTime lastUpdated;

    @OneToOne
    private VehicleOwner owner;

    @ManyToOne
    private Garage garage;
}
