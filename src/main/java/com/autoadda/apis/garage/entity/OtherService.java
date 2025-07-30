package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class OtherService {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "company_brand_name")
    private String companyBrandName;

    @Column(name = "company_arabic_name")
    private String companyArabicName;

    @Column(name = "company_name")
    private String name;

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
    @JoinColumn(name = "company_logo")
    private MediaFile companyLogo;

    @OneToOne
    private Location companyLocation;

    @Column(name = "account_status")
    private Status accountStatus;

    @Column(name = "service_type")
    private String serviceType;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "company_owner_id")
    private Integer companyOwner;

    private boolean isActive;

    @OneToMany
    @JoinColumn(name = "company_trade_license")
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<> ( );

    @OneToMany
    @JoinColumn(name = "company_vat_certificate")
    private List<MediaFile> vatCertificate = new ArrayList<> ( );


    @OneToMany
    @JoinColumn(name = "company_images")
    private List<MediaFile> companyImages = new ArrayList<> ( );

    @PrePersist
    public void prePersist() {
        isActive = true;
        createdAt = LocalDateTime.now ( );
    }

}
