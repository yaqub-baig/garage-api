package com.autoadda.apis.garage.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GarageImportRequest {

    // Apify field: "title"
    private String title;

    // Apify field: "phoneUnformatted" or "phone"
    private String phone;

    // Apify field: "website"
    private String website;

    // Apify field: "url" (Google Maps link)
    private String gmapUrl;

    // Apify field: "address"
    private String address;

    // Apify field: "neighborhood"
    private String neighborhood;

    // Apify field: "city"
    private String city;

    // Apify field: "state"
    private String state;

    // Apify field: "countryCode"
    private String countryCode;

    // Apify field: "totalScore"
    private Double totalScore;

    // Apify field: "reviewsCount"
    private Integer reviewsCount;

    // Apify field: "categoryName" (primary Google category)
    private String categoryName;

    // Apify field: "categories" (all applicable categories)
    private List<String> categories;
}
