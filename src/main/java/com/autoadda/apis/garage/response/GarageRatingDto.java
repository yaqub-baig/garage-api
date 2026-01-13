package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GarageRatingDto {
    private Integer id;
    private Integer garageId;
    private String garageName;
    private Integer workOrderId;
    private Integer customerId;
    private UserResponse customer;
    private String customerName;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OwnerResponseDto ownerResponse;
}
