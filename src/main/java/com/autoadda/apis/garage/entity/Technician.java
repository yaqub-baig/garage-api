package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "emirates_id")
    private String emiratesId;

    @Column(name = "auto_adda_id")
    private Integer autoAddaId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "mobile_number",unique = true)
    private String mobileNumber;

    @Column(name = "vat_number")
    private String vatNumber;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "technician_skill_set",
            joinColumns = @JoinColumn(name = "technician_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_set_id")
    )
    private List<AvailableService> skillSet = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @Column(name = "profile_img")
    private String profileImg;

    @Enumerated(EnumType.STRING)
    private EMPLOYEE_ROLE employeeRole;

    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        createdAt = LocalDateTime.now ( );
    }
}
