package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GarageFavoriteResponse {
    private Integer id;
    private Integer vehicleOwnerId; // The owner adding to favorites

    private GarageResponse favoriteGarage; // The garage being added
    private LocalDateTime addedAt;
}
