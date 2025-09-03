package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.config.AuthClient;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.response.VehicleOwnerSubscriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VehicleOwnerSubscriptionMapper {
    private final AuthClient authClient;
    private final GarageSubscriptionMapper garageSubscriptionMapper;

    public VehicleOwnerSubscriptionResponse toResponse(VehicleOwnerSubscription vehicleOwnerSubscription) {
        VehicleOwnerSubscriptionResponse vehicleOwnerSubscriptionResponse = new VehicleOwnerSubscriptionResponse ( );
        vehicleOwnerSubscriptionResponse.setId ( vehicleOwnerSubscription.getId ( ) );
        vehicleOwnerSubscriptionResponse.setCustomer ( authClient.getUser ( vehicleOwnerSubscription.getCustomerId ( ) ) );
        vehicleOwnerSubscriptionResponse.setGarageSubscription ( garageSubscriptionMapper.toResponse ( vehicleOwnerSubscription.getGarageSubscription ( ) ) );
        vehicleOwnerSubscriptionResponse.setStatus ( vehicleOwnerSubscription.getStatus ( ) );
        vehicleOwnerSubscriptionResponse.setPurchaseDate ( vehicleOwnerSubscription.getPurchaseDate ( ) );
        vehicleOwnerSubscriptionResponse.setStartDate ( vehicleOwnerSubscription.getStartDate ( ) );
        vehicleOwnerSubscriptionResponse.setEndDate ( vehicleOwnerSubscription.getEndDate ( ) );
        vehicleOwnerSubscriptionResponse.setUsageCounts ( vehicleOwnerSubscription.getUsageCounts ( ) );
        return vehicleOwnerSubscriptionResponse;
    }
}
