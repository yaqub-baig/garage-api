package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.response.GarageSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GarageSubscriptionMapper {

    private final AvailableServiceMapper availableServiceMapper;

    public GarageSubscriptionResponse toResponse(GarageSubscription garageSubscription) {
        GarageSubscriptionResponse garageSubscriptionResponse = new GarageSubscriptionResponse ( );
        garageSubscriptionResponse.setId ( garageSubscription.getId ( ) );
        garageSubscriptionResponse.setName ( garageSubscription.getName ( ) );
        garageSubscriptionResponse.setDescription ( garageSubscription.getDescription ( ) );
        garageSubscriptionResponse.setPrice ( garageSubscription.getPrice ( ) );
        garageSubscriptionResponse.setDurationMonths ( garageSubscription.getDurationMonths ( ) );
        garageSubscriptionResponse.setBenefits ( garageSubscription.getBenefits ( ) );
        garageSubscriptionResponse.setBrands ( garageSubscription.getBrands ( ) );
        garageSubscriptionResponse.setServiceQuantity ( garageSubscription.getServiceQuantity ( ) );
        garageSubscriptionResponse.setServices ( garageSubscription.getServicesIncluded ( ).stream ( ).map ( availableServiceMapper::toResponse ).toList ( ) );
        return garageSubscriptionResponse;
    }
}
