package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.Status;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TechnicianRequest {

    private String emiratesId;

    private String firstName;

    private String emailId;

    private String mobileNumber;

    private List<Integer> skillSetIds;

    private Status status;

    @Deprecated(since = "1.0", forRemoval = true)
    private Integer garageId;  // Deprecated: Use garageIds instead

    private List<Integer> garageIds;  // Support for many-to-many garage relationship

    private EMPLOYEE_ROLE employeeRole;

    private String profileImg;

    private String brand;

    private String model;

    private String description;

    private Integer yearOfExperience;

    private String city;

    private Boolean isAvailableForService;

}
