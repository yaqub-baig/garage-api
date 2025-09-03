package com.autoadda.apis.garage.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class FavoriteGarageResponse {
    private Integer id;
    private VehicleDetailResponse vehicleOwner; // The owner adding to favorites
    private GarageResponse favoriteGarage; // The garage being added
    private LocalDateTime addedAt;
}
