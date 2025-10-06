package com.autoadda.apis.garage.response.os;

import com.autoadda.apis.garage.response.AvailableServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherServiceResponse {
    private Integer id;
    private Boolean isActive;
    private Integer osServiceId;
    private AvailableServiceResponse availableService; // Using the existing AvailableServiceResponse
}
