package com.autoadda.apis.garage.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** vehicle-api's GenericResponse envelope with a typed body. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleApiResponse<T> {
    private String status;
    private String code;
    private String message;
    private T body;
}
