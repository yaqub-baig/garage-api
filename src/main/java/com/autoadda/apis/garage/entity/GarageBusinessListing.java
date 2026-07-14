package com.autoadda.apis.garage.entity;

import com.autoadda.apis.garage.util.GarageListingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "garage_business_listing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageBusinessListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "reason_for_selling", length = 1000)
    private String reasonForSelling;

    @Column(name = "asking_price", nullable = false)
    private Double askingPrice;

    @Column(name = "is_negotiable")
    @Builder.Default
    private Boolean isNegotiable = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GarageListingStatus status;

    @Column(name = "listed_date")
    private LocalDate listedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany
    @JoinColumn(name = "listing_images")
    @Builder.Default
    private List<MediaFile> listingImages = new ArrayList<>();

    @OneToOne(mappedBy = "listing", cascade = CascadeType.ALL)
    private GarageBusinessSale sale;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (listedDate == null) {
            listedDate = LocalDate.now();
        }
        if (status == null) {
            status = GarageListingStatus.ACTIVE;
        }
        if (isNegotiable == null) {
            isNegotiable = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
