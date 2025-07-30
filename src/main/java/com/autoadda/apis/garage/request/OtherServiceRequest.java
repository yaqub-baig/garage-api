package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
