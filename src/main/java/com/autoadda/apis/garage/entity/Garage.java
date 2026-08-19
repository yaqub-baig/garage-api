package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.GarageVerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    @Column(name = "trade_licence_number", unique = true)
    private String tradeLicenceNumber;

    @Column(name = "opening_time")
    private String openingTime;

    @Column(name = "closing_time")
    private String closingTime;

    @Column(name = "opening_time_2")
    private String openingTime2;

    @Column(name = "closing_time_2")
    private String closingTime2;

    @Column(name = "break_start_time")
    private String breakStartTime;

    @Column(name = "break_end_time")
    private String breakEndTime;

    @Column(name = "working_days")
    private String workingDays;

    @Column(name = "trn_number")
    private String trnNumber;

    @Column(name = "website", length = 2048)
    private String website;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private GarageVerificationStatus accountStatus;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_by")
    private Integer rejectedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    @Column(name = "resubmission_count")
    @Builder.Default
    private Integer resubmissionCount = 0;

    @Column(name = "tamara_payment_available")
    @Builder.Default
    private Boolean tamaraPaymentAvailable = false;

    @Column(name = "last_reviewed_by")
    private Integer lastReviewedBy;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "brands", columnDefinition = "LONGTEXT")
    private String brands;

    @Column(name = "models", columnDefinition = "LONGTEXT")
    private String models;

    @Column(name = "garage_owner_id")
    private Integer garageOwner;

    private boolean isActive;

    private Integer concurrentAppointmentCapacity;

    @Column(name = "gmap_rating")
    private Double gmapRating;

    @Column(name = "gmap_reviews_count")
    private Integer gmapReviewsCount;

    @Column(name = "gmap_category")
    private String gmapCategory;

    @OneToMany
    @JoinColumn(name = "garage_trade_license")
    @Builder.Default
    private List<MediaFile> tradeLicenseAttachment = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "garage_vat_certificate")
    @Builder.Default
    private List<MediaFile> vatCertificate = new ArrayList<>();


    @OneToMany(mappedBy = "garage", cascade = CascadeType.ALL)
    @Builder.Default
    private List<GarageService> services = new ArrayList<>();

    @ManyToMany(mappedBy = "garages")
    @JsonIgnore
    @Builder.Default
    private List<Technician> technicians = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "garage_images")
    @Builder.Default
    private List<MediaFile> garageImages = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        isActive = true;
        createdAt = LocalDateTime.now();
        if (resubmissionCount == null) {
            resubmissionCount = 0;
        }
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
