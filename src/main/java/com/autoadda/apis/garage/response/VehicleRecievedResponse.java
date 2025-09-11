package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.entity.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class VehicleRecievedResponse {
    private Integer id;
    private VehicleDetailResponse vehicle;
    private AppointmentDetailResponse appointment;
    private TechnicianResponse recievedBy;
    private LocalDateTime recievedTime;
    private String comment;
    private List<MediaFile> vehicleRecievedImages;


}
