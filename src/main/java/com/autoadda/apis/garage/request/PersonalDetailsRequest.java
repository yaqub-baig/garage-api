package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.Gender;
import com.autoadda.apis.garage.util.MaritalStatus;
import com.autoadda.apis.garage.util.VisaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Step 1 of the technician CV profile. Every field is optional;
 * a null field leaves the stored value unchanged.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetailsRequest {

    // Stored on Technician
    private String firstName;
    private String lastName;
    private String emailId;
    private String mobileNumber;
    private String city;
    private String profileImg;
    private Integer yearOfExperience;

    // Stored on TechnicianProfile
    private LocalDate dateOfBirth;
    private Gender gender;
    private String nationality;
    private MaritalStatus maritalStatus;
    private String alternatePhone;
    private String addressLine;
    private String country;
    private String passportNumber;
    private LocalDate passportExpiry;
    private VisaStatus visaStatus;
    private LocalDate visaExpiry;
    private String drivingLicenseNumber;
    private String professionalSummary;
}
