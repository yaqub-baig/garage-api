package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.response.GarageSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GarageSubscriptionMapper {

    private final AvailableServiceMapper availableServiceMapper;
    private final GarageServiceMapper garageServiceMapper;

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

        // Filter garage services to only include services that are in the subscription plan
        Set<Integer> includedServiceIds = garageSubscription.getServicesIncluded()
                .stream()
                .map(AvailableService::getId)
                .collect(Collectors.toSet());

        garageSubscriptionResponse.setGarageServices(
                garageSubscription.getGarage().getServices().stream()
                        .filter(garageService -> garageService.getAvailableService() != null
                                && includedServiceIds.contains(garageService.getAvailableService().getId()))
                        .map(garageServiceMapper::entityToResponse)
                        .toList()
        );

        garageSubscriptionResponse.setGarageLocation ( garageSubscription.getGarage ( ).getGarageLocation() );
        garageSubscriptionResponse.setGarageId(garageSubscription.getGarage().getId());
        garageSubscriptionResponse.setIsActive ( garageSubscription.getIsActive ( ) );
        garageSubscriptionResponse.setGarageName ( garageSubscription.getGarage ( ).getName ( ) );
        return garageSubscriptionResponse;
    }
}
