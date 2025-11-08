package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePartRequestRequest {

    private Integer jobCardId;
    private Integer technicianId;
    private List<PartRequestItemDto> items;
    private String notes;
}
