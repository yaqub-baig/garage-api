package com.autoadda.apis.garage.entity;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageWorkingHours {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@ManyToOne
	@JoinColumn(name = "garage_id", referencedColumnName = "id")
	private Garage garage;
	@Enumerated(EnumType.STRING)
	private DayOfWeek dayOfWeek;
	private LocalTime openTime;
	private LocalTime closeTime;
	private boolean isClosed;
	final private int slotDurationMinutes = 20;
}