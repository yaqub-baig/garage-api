package com.autoadda.apis.garage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "technician_reference", indexes = @Index(name = "idx_tech_reference_technician", columnList = "technician_id"))
@Getter
@Setter
@NoArgsConstructor
public class TechnicianReference extends TechnicianProfileItem {

    @Column(name = "name")
    private String name;

    @Column(name = "company")
    private String company;

    @Column(name = "designation")
    private String designation;

    @Column(name = "relationship")
    private String relationship;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;
}
