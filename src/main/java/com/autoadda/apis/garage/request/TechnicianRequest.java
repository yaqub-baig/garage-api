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

    private String lastName;

    private String emailId;

    private String mobileNumber;

    private String vatNumber;

    private List<Integer> skillSetIds;

    private Status status;

    private Integer garageId;

    private EMPLOYEE_ROLE employeeRole;

    private String profileImg;

}
