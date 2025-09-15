package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.util.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtherServiceResponse {
    private Integer id;
    private Boolean isActive;
    private Integer osServiceId;
    private AvailableServiceResponse availableService; // Using the existing AvailableServiceResponse
}
