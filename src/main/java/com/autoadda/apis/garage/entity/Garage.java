package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Garage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "garage_brand_name")
    private String garageBrandName;

    @Column(name = "garage_arabic_name")
    private String garageArabicName;

    @Column(name = "garage_name")
    private String name;

    @Column(name ="trade_licence_number", unique = true)
    private String tradeLicenceNumber;

    @Column(name ="opening_time")
    private String openingTime;

    @Column(name ="closing_time")
    private String closingTime;

    @Column(name ="break_start_time")
    private String breakStartTime;

    @Column(name ="break_end_time")
    private String breakEndTime;

    @Column(name = "working_days")
    private String workingDays;

    @Column(name = "trn_number")
    private String trnNumber;

    @Column(name = "website")
    private String webSite;

    @Column(name = "email_id")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = " mobile_number")
    private String mobileNumber;

    @OneToOne
    @JoinColumn(name = "garage_logo")
    private MediaFile garageLogo;

    @ManyToOne
    private Location garageLocation;

    @Column(name = "account_status")
    private Status accountStatus;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "brands")
    private String brands;

    @Column(name = "models")
    private String models;

    @Column(name = "garage_owner_id")
    private Integer garageOwner;

    private boolean isActive;

    private Integer concurrentAppointmentCapacity;

    @OneToMany
    @JoinColumn(name = "garage_trade_license")
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<> ( );

    @OneToMany
    @JoinColumn(name = "garage_vat_certificate")
    private List<MediaFile> vatCertificate = new ArrayList<> ( );


    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GarageService> services = new ArrayList<> ( );

    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Technician> technicians = new ArrayList<> ( );

    @OneToMany
    @JoinColumn(name = "garage_images")
    private List<MediaFile> garageImages = new ArrayList<> ( );

    @PrePersist
    public void prePersist() {
        isActive = true;
        createdAt = LocalDateTime.now ( );
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Garage)) return false;
        Garage garage = (Garage) o;
        return Objects.equals(id, garage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
