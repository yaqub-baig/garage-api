package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.response.GarageServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GarageServiceMapper {

    private final AvailableServiceMapper availableServiceMapper;

    public GarageServiceResponse entityToResponse(GarageService garageService) {
        GarageServiceResponse garageServiceResponse = new GarageServiceResponse ( );
        garageServiceResponse.setId ( garageService.getId ( ) );
        garageServiceResponse.setIsActive ( garageService.getIsActive ( ) );
        garageServiceResponse.setGarageId ( garageService.getGarage ( ).getId ( ) );
        garageServiceResponse.setPrice ( garageService.getPrice ( ) );
        garageServiceResponse.setLaborPrice ( garageService.getLabourPrice ( ) );
        garageServiceResponse.setAvailableService ( availableServiceMapper.toResponse ( garageService.getAvailableService ( ) ) );
        return garageServiceResponse;
    }
}
