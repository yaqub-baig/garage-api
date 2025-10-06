package com.autoadda.apis.garage.response.os;

import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.response.GarageServiceResponse;
import com.autoadda.apis.garage.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OsResponse {
    private Integer id;
    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String tradeLicenceNumber;
    private String openingTime;
    private String closingTime;
    private String breakStartTime;
    private String breakEndTime;
    private String workingDays;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private Location compnayLocation;
    private MediaFile companyLogo;  // Directly using MediaFile entity
    private Status accountStatus;
    private LocalDateTime createdAt;
    private String brands;
    private String models;
    private Integer companyOwnerId;
    private Boolean isActive;
    private Integer concurrentAppointmentCapacity;
    private List<GarageServiceResponse> services;
    private List<MediaFile> companyImages;
    private List<MediaFile> vatCertificate;
    private List<MediaFile> tradeLicenseAttachment;
}
