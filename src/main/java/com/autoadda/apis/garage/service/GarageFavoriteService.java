package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageFavorite;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageFavoriteRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GarageFavoriteService {

    private final GarageFavoriteRepository garageFavRepo;
    private final GarageRepository garageRepository;

    public GarageFavorite addFavoriteGarage(Integer vehicleOwnerId, Integer favGarageId) {
        if ( garageFavRepo.existsByVehicleOwnerIdAndFavoriteGarageId ( vehicleOwnerId, favGarageId ) ) {
            throw new ResourceNotFoundException ( "Already marked as favorite" );
        }
        GarageFavorite fav = new GarageFavorite ( );
        fav.setVehicleOwnerId ( vehicleOwnerId );
        Garage garage = garageRepository.findById ( favGarageId ).orElseThrow ( () -> new ResourceNotFoundException ( "no garage found" ) );
        fav.setFavoriteGarage ( garage );
        fav.setAddedAt ( LocalDateTime.now ( ) );
        return garageFavRepo.save ( fav );
    }

    public List<GarageFavorite> getFavoriteGarages(Integer garageOwnerId) {
        return garageFavRepo.findByVehicleOwnerId ( garageOwnerId );
    }

    public void removeFavoriteGarage(Integer garageOwnerId, Integer favGarageId) {
        garageFavRepo.findAll ( ).stream ( ).filter (
                        fav -> fav.getVehicleOwnerId ( ).equals ( garageOwnerId ) && fav.getFavoriteGarage ( ).getId ( ).equals ( favGarageId ) )
                .findFirst ( ).ifPresent ( garageFavRepo::delete );
    }

}
