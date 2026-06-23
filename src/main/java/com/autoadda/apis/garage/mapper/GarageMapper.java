package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.repository.GarageRatingRepository;
import com.autoadda.apis.garage.response.GarageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class GarageMapper {
    private final GarageServiceMapper garageServiceMapper;
    private final GarageRatingRepository garageRatingRepository;

    public GarageResponse entityToResponse(Garage garage) {
        GarageResponse garageResponse = new GarageResponse();

        mapField(garage.getId(), garageResponse::setId);
        mapField(garage.getGarageBrandName(), garageResponse::setGarageBrandName);
        mapField(garage.getGarageArabicName(), garageResponse::setGarageArabicName);
        mapField(garage.getName(), garageResponse::setName);
        mapField(garage.getTradeLicenceNumber(), garageResponse::setTradeLicenceNumber);
        mapField(garage.getOpeningTime(), garageResponse::setOpeningTime);
        mapField(garage.getClosingTime(), garageResponse::setClosingTime);
        mapField(garage.getWorkingDays(), garageResponse::setWorkingDays);
        mapField(garage.getOpeningTime2(), garageResponse::setOpeningHours2);
        mapField(garage.getClosingTime2(), garageResponse::setClosingHours2);
        mapField(garage.getTrnNumber(), garageResponse::setTrnNumber);
        mapField(garage.getWebsite(), garageResponse::setWebsite);
        mapField(garage.getEmail(), garageResponse::setEmail);
        mapField(garage.getPhoneNumber(), garageResponse::setPhoneNumber);
        mapField(garage.getMobileNumber(), garageResponse::setMobileNumber);
        mapField(garage.getGarageLogo(), garageResponse::setGarageLogo);
        mapField(garage.getAccountStatus(), garageResponse::setAccountStatus);
        mapField(garage.getApprovedBy(), garageResponse::setApprovedBy);
        mapField(garage.getApprovedAt(), garageResponse::setApprovedAt);
        mapField(garage.getRejectedBy(), garageResponse::setRejectedBy);
        mapField(garage.getRejectedAt(), garageResponse::setRejectedAt);
        mapField(garage.getRejectionReason(), garageResponse::setRejectionReason);
        mapField(garage.getLastReviewedBy(), garageResponse::setLastReviewedBy);
        mapField(garage.getLastReviewedAt(), garageResponse::setLastReviewedAt);
        mapField(garage.getCreatedAt(), garageResponse::setCreatedAt);
        mapField(garage.getBrands(), garageResponse::setBrands);
        mapField(garage.getModels(), garageResponse::setModels);
        mapField(garage.getGarageOwner(), garageResponse::setGarageOwnerId);
        mapField(garage.getConcurrentAppointmentCapacity(), garageResponse::setConcurrentAppointmentCapacity);
        mapField(garage.getGarageImages(), garageResponse::setGarageImages);
        mapField(garage.getTradeLicenseAttachment(), garageResponse::setTradeLicenseAttachment);
        mapField(garage.getVatCertificate(), garageResponse::setVatCertificate);
        mapField(garage.getGarageLocation(), garageResponse::setLocation);
        mapField(garage.getBreakStartTime(), garageResponse::setBreakStartTime);
        mapField(garage.getBreakEndTime(), garageResponse::setBreakEndTime);
        mapField(garage.getGmapRating(), garageResponse::setGmapRating);
        mapField(garage.getGmapReviewsCount(), garageResponse::setGmapReviewsCount);
        mapField(garage.getGmapCategory(), garageResponse::setGmapCategory);

        // Primitive boolean - no null check needed
        garageResponse.setIsActive(garage.isActive());

        // Field with default value
        garageResponse.setResubmissionCount(
            garage.getResubmissionCount() != null ? garage.getResubmissionCount() : 0);

        // Field with transformation
        mapField(garage.getServices(),
            services -> garageResponse.setServices(
                services.stream().map(garageServiceMapper::entityToResponse).toList()));

        // Populate rating fields
        if (garage.getId() != null) {
            Double avgRating = garageRatingRepository.calculateAverageRatingByGarageId(garage.getId());
            if (avgRating != null) {
                garageResponse.setAverageRating(Math.round(avgRating * 10.0) / 10.0);
            }
            garageResponse.setTotalRatings(garageRatingRepository.countByGarageIdAndIsVisibleTrue(garage.getId()));
        }

        return garageResponse;
    }

    /**
     * Helper method to map a field with null safety.
     * Only sets the value if source is not null.
     *
     * @param source The source value (may be null)
     * @param setter The setter method to call if source is not null
     * @param <T> The type of the field
     */
    private <T> void mapField(T source, java.util.function.Consumer<T> setter) {
        if (source != null) {
            setter.accept(source);
        }
    }

}
