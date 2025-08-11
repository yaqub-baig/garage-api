package com.service.garage.apis.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvailableServiceRequest {

    private Integer id;

    private String name;

    private String description;

    private Double price;

    private Double laborPrice;


}
