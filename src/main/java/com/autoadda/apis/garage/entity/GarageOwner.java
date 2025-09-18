package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GarageOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "emirates_id")
    private String emiratesId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "vat_number")
    private String vatNumber;

    @Column(name = "account_status")
    private Status accountStatus;

//	@OneToMany(mappedBy = "garageOwner", cascade = CascadeType.ALL, orphanRemoval = true)
//	private List<Garage> ownedGarages = new ArrayList<>();

    @Column(name = "insert_tms")
    private LocalDate insertTms;
}
