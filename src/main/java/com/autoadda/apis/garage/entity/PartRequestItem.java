package com.autoadda.apis.garage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "part_request_id", nullable = false)
    private PartRequest partRequest;

    @Column(name = "part_name", nullable = false)
    private String partName;

    @Column(name = "part_number")
    private String partNumber;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "estimated_price")
    private Double estimatedPrice;

    @OneToOne(mappedBy = "partRequestItem", cascade = CascadeType.ALL)
    private GarageProcurement garageProcurement;
}
