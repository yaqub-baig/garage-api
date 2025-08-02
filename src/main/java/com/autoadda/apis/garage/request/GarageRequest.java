package com.autoadda.apis.garage.request;

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
    private String openingTime;
    private String closingTime;
    private String tradeLicenceNumber;
    private String workingDays;
    private String mobileNumber;
    private LocationRequest garageLocation;
    private String garageType;
    private Integer chainId;
    private String brands;
    private String models;
    private Long logoId;
    private List<Long> vatCertificateIds;
    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> garageImagesIds;

}
