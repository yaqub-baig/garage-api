package com.autoadda.apis.garage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "is_active")
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "garage_id")
    @JsonIgnore
    private Garage garage;

    @ManyToOne
    @JoinColumn(name = "service_id")
    @JsonIgnore
    private AvailableService availableService;

    private Double price;

    private Double labourPrice;
}
