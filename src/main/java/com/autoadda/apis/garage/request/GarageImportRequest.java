package com.autoadda.apis.garage.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GarageImportRequest {

    private String title;

    private String phone;

    private String website;

    @JsonProperty("url")
    private String gmapUrl;

    private String address;

    private String neighborhood;

    private String city;

    private String state;

    private String countryCode;

    private Double totalScore;

    private Integer reviewsCount;

    private String categoryName;

    private List<String> categories;
}
