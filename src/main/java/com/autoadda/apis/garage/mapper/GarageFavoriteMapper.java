package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.GarageFavorite;
import com.autoadda.apis.garage.response.GarageFavoriteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GarageFavoriteMapper {

private final GarageMapper garageMapper;
    public GarageFavoriteResponse toResponse(GarageFavorite save) {
       GarageFavoriteResponse garageFavoriteResponse=new GarageFavoriteResponse();
       garageFavoriteResponse.setFavoriteGarage(garageMapper.entityToResponse(save.getFavoriteGarage()));
       garageFavoriteResponse.setVehicleOwnerId(save.getVehicleOwnerId());
       garageFavoriteResponse.setId(save.getId());

        return garageFavoriteResponse;
    }
}
