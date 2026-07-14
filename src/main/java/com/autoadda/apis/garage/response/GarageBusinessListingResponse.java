package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.GarageListingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageBusinessListingResponse {
    private Integer id;
    private Integer garageId;
    private String garageName;
    private String title;
    private String description;
    private String reasonForSelling;
    private Double askingPrice;
    private Boolean isNegotiable;
    private GarageListingStatus status;
    private LocalDate listedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ListingImage> listingImages;
    private Integer saleId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListingImage {
        private Long id;
        private String url;
        private String fileName;
    }
}
