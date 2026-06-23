package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.util.GarageVerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GarageResponse {
    private Integer id;
    private String garageBrandName;
    private String garageArabicName;
    private String name;
    private String tradeLicenceNumber;
    private String openingTime;
    private String closingTime;
    private String closingHours2;
    private String openingHours2;
    private String breakStartTime;
    private String breakEndTime;
    private String workingDays;
    private String trnNumber;
    private String website;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private Location location;
    private MediaFile garageLogo;  // Directly using MediaFile entity
    private GarageVerificationStatus accountStatus;
    private Integer approvedBy;
    private LocalDateTime approvedAt;
    private Integer rejectedBy;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private Integer resubmissionCount;
    private Integer lastReviewedBy;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime createdAt;
    private String brands;
    private String models;
    private Integer garageOwnerId;
    private Boolean isActive;
    private Integer concurrentAppointmentCapacity;
    private List<GarageServiceResponse> services;
    private List<MediaFile> garageImages;
    private List<MediaFile> vatCertificate;
    private List<MediaFile> tradeLicenseAttachment;
    private Double averageRating;
    private Long totalRatings;
    private Double gmapRating;
    private Integer gmapReviewsCount;
    private String gmapCategory;
}
