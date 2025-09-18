package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class GarageTechnician {

    private Integer id;

    private String emiratesId;

    private String firstName;

    private String lastName;

    private String emailId;

    private String mobileNumber;

    private String vatNumber;

    private Status accountStatus;

    private String profileImg;

    private Integer garageId;

    private Date insertTms;
}
