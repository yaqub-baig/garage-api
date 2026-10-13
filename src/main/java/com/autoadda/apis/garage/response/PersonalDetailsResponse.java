package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Gender;
import com.autoadda.apis.garage.util.MaritalStatus;
import com.autoadda.apis.garage.util.VisaStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalDetailsResponse {

    private String firstName;
    private String lastName;
    private String emailId;
    private String mobileNumber;
    private String city;
    private String profileImg;
    private Integer yearOfExperience;
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
