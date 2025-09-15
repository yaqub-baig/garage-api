package com.autoadda.apis.garage.entity.os;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OsAppointmentServices {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "status")
    private Status status;

    @Column(name = "appointment_id")
    private Integer appointmentId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "os_service_id", referencedColumnName = "id")
    private OsService osService;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "technician_id", referencedColumnName = "id")
    private Technician technician;

}
