package com.autoadda.apis.garage.entity.os;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OsHolidayOverride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "other_service_id", referencedColumnName = "id")
    private OtherService otherService;
    private LocalDate date;
    private boolean isClosed;
    private LocalTime openTime;
    private LocalTime closeTime;
}