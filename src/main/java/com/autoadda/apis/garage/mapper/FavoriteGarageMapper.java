package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.config.VehicleClient;
import com.autoadda.apis.garage.entity.GarageFavorite;
import com.autoadda.apis.garage.response.FavoriteGarageResponse;
import com.autoadda.apis.garage.response.GarageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteGarageMapper {
    private final VehicleClient vehicleClient;
    private final GarageMapper garageMapper;
     public FavoriteGarageResponse toResponse(GarageFavorite favorite){
         FavoriteGarageResponse favoriteGarageResponse = new FavoriteGarageResponse ( );
         favoriteGarageResponse.setId(favorite.getId());
         favoriteGarageResponse.setVehicleOwner(vehicleClient.getVehicleDetailsById(favorite.getVehicleOwnerId ()));
         favoriteGarageResponse.setFavoriteGarage(garageMapper.entityToResponse ( favorite.getFavoriteGarage () ));
         favoriteGarageResponse.setAddedAt(favorite.getAddedAt());
         return favoriteGarageResponse;
     }

}
