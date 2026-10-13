package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceResponse {

    private Integer id;
    private Integer displayOrder;
    private String name;
    private String company;
    private String designation;
    private String relationship;
    private String phone;
    private String email;
}
