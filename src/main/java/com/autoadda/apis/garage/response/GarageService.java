package com.autoadda.apis.garage.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageService {
    private Integer id;

    private String service;

    private String description;

    private Integer categoryId;

    private Date insertTms;

}
