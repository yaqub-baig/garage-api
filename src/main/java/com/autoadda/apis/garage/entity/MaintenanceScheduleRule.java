package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.IntervalType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceScheduleRule {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	private Integer id;

    private String make;
    private String model;
    private String serviceType;

    @Enumerated(EnumType.STRING)
    private IntervalType intervalType; // TIME or MILEAGE

    private Integer intervalValue; // e.g. 6 (months) or 10000 (km)

    private boolean isActive;
}
