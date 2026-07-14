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
public class UpdateGarageBusinessListingRequest {
    private String title;
    private String description;
    private String reasonForSelling;
    private Double askingPrice;
    private Boolean isNegotiable;
    private List<Long> listingImageIds;
}
