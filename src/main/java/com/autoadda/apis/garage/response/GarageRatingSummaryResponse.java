package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageRatingSummaryResponse {
    private Integer garageId;
    private Double averageRating;
    private Long totalRatings;
    private Map<Integer, Long> ratingDistribution;
}
