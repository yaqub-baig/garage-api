package com.autoadda.apis.garage.entity.os;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OsBreakTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "other_service_id", referencedColumnName = "id")
    private OtherService otherService;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    private LocalTime breakStart;
    private LocalTime breakEnd;
}