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
public class OtherServiceRequest {
    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private LocationRequest companyLocation;
    private String serviceType;
    private String tradeLicenceNumber;
    private String openingTime;
    private String closingTime;
    private String workingDays;
    private Long logoId;
    private List<Long> vatCertificateIds;
    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> companyImagesIds;
}
