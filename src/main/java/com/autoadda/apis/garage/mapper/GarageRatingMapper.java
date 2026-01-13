package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.GarageRating;
import com.autoadda.apis.garage.entity.GarageRatingResponse;
import com.autoadda.apis.garage.response.GarageRatingDto;
import com.autoadda.apis.garage.response.OwnerResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GarageRatingMapper {
    private final AuthClient authClient;
    public GarageRatingDto entityToDto(GarageRating entity) {
        if (entity == null) {
            return null;
        }

        GarageRatingDto dto = GarageRatingDto.builder()
                .id(entity.getId())
                .garageId(entity.getGarage() != null ? entity.getGarage().getId() : null)
                .garageName(entity.getGarage() != null ? entity.getGarage().getName() : null)
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getId() : null)
                .customerId(entity.getCustomerId())
                .customer(authClient.getUser(entity.getCustomerId()))
                .rating(entity.getRating())
                .reviewText(entity.getReviewText())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (entity.getOwnerResponse() != null) {
            dto.setOwnerResponse(ownerResponseToDto(entity.getOwnerResponse()));
        }

        return dto;
    }

    public OwnerResponseDto ownerResponseToDto(GarageRatingResponse entity) {
        if (entity == null) {
            return null;
        }

        return OwnerResponseDto.builder()
                .id(entity.getId())
                .responderId(entity.getResponderId())
                .responseText(entity.getResponseText())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
