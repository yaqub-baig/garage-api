package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarageRequest {
    private String garageBrandName;
    private String garageArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private MediaFile garageLogo;
    private LocationRequest garageLocation;
    private String garageType;
    private Integer chainId;
    private String brands;
    private String models;
    private Integer garageOwnerId;
    private List<MediaFile> tradeLicenseAttachment;
    private List<MediaFile> vatCertificate;
    private List<MediaFile> garageImages;
}
