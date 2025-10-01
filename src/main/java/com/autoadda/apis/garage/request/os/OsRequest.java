package com.autoadda.apis.garage.request.os;

import com.autoadda.apis.garage.request.LocationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OsRequest {
    private String companyBrandName;
    private String companyArabicName;
    private String name;
    private String trnNumber;
    private String webSite;
    private String email;
    private String phoneNumber;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime openingTime;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime closingTime;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime breakStartTime;
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime breakEndTime;
    private Integer concurrentAppointmentCapacity;
    private String tradeLicenceNumber;
    private List<DayOfWeek> workingDays;
    private String mobileNumber;
    private LocationRequest otherServiceLocation;
    private List<Integer> OsServiceIds;
    private String brands;
    private String models;
    private Long logoId;
    private List<Long> vatCertificateIds;
    private List<Long> tradeLicenseAttachmentIds;
    private List<Long> companyImagesIds;

}
