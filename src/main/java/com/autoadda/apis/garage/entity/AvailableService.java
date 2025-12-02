package com.autoadda.apis.garage.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableService {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Double price;

    @Column(name = "labor_price")
    private Double laborPrice;

    @OneToMany(mappedBy = "availableService", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GarageService> garageLinks = new ArrayList<> ( );

}
