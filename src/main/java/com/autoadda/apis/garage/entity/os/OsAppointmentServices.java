package com.autoadda.apis.garage.entity.os;

import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.*;

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
