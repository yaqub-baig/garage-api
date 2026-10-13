package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceRequest implements ProfileItemRequest {

    private Integer id;
    private String name;
    private String company;
    private String designation;
    private String relationship;
    private String phone;
    private String email;
}
