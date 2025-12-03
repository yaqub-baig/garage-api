package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.VehicleFavorite;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.repository.VehicleFavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleFavoriteService {

    private final VehicleFavoriteRepository vehicleFavRepo;

    public VehicleFavorite addFavoriteVehicle(Integer vehicleOwnerId, Integer favVehicleId) {
        if ( vehicleFavRepo.existsByVehicleOwnerIdAndFavoriteVehicleId ( vehicleOwnerId, favVehicleId ) ) {
            throw new AlreadyExistsException( "Already marked as favorite" );
        }
        VehicleFavorite fav = new VehicleFavorite ( );
        fav.setVehicleOwnerId ( vehicleOwnerId );
        fav.setFavoriteVehicleId ( favVehicleId );
        fav.setAddedAt ( LocalDateTime.now ( ) );
        return vehicleFavRepo.save ( fav );
    }

    public List<VehicleFavorite> getFavoriteVehicles(Integer vehicleOwnerId) {
        return vehicleFavRepo.findByVehicleOwnerId ( vehicleOwnerId );
    }

    public void removeFavoriteVehicle(Integer vehicleOwnerId, Integer favVehicleId) {
        vehicleFavRepo.findAll ( ).stream ( )
                .filter ( fav -> fav.getVehicleOwnerId ( ).equals ( vehicleOwnerId )
                        && fav.getFavoriteVehicleId ( ).equals ( favVehicleId ) )
                .findFirst ( ).ifPresent ( vehicleFavRepo::delete );
    }
}
