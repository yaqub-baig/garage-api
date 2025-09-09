package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TechnicianResponse {
    private Integer id;
    private String emiratesId;
    private UserResponse autoAddaUser;
    private String firstName;
    private String lastName;
    private String emailId;
    private String mobileNumber;
    private String vatNumber;
    private List<AvailableServiceResponse> skillSet;
    private Status status;
    private GarageResponse garage;
    private String profileImg;
    private EMPLOYEE_ROLE employeeRole;
}
