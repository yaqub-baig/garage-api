package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageBasicInfo {

    private Integer id;
    private String garageBrandName;
    private String name;
    private String phoneNumber;
    private String mobileNumber;
    private Location location;
}
