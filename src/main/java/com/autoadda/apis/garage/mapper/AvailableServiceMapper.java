package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.response.AvailableServiceResponse;
import org.springframework.stereotype.Component;

@Component
public class AvailableServiceMapper {
    public AvailableServiceResponse toResponse(AvailableService availableService) {
        AvailableServiceResponse availableServiceResponse = new AvailableServiceResponse ( );
        availableServiceResponse.setId ( availableService.getId ( ) );
        availableServiceResponse.setName ( availableService.getName ( ) );
        availableServiceResponse.setDescription ( availableService.getDescription ( ) );
        availableServiceResponse.setPrice ( availableService.getPrice ( ) );
        availableServiceResponse.setLaborPrice ( availableService.getLaborPrice ( ) );
        return availableServiceResponse;
    }
}
