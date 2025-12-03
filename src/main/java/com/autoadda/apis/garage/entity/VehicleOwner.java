package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.OwnerType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "owner_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String email;

    private String mobileNumber;

    private int loyaltyPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", insertable = false, updatable = false)
    private OwnerType ownerType;

    @ManyToOne
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private Location location;

    @OneToMany(mappedBy = "vehicleOwner", cascade = CascadeType.ALL)
    private List<VehicleDetails> vehicleDetails;

    @ManyToOne
    @JoinColumn(name = "favorite_garage_id")
    private Garage favoriteGarage;
}
