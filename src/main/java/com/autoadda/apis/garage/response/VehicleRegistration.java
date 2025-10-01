package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRegistration {
    private Integer id;

    private Status state;

    private String code;

    private String category;

    private String plateNumber;

    private String registeredName;
}
