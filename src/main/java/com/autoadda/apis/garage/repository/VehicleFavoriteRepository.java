package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleFavoriteRepository extends JpaRepository<VehicleFavorite, Integer> {
    List<VehicleFavorite> findByVehicleOwnerId(Integer ownerId);

    boolean existsByVehicleOwnerIdAndFavoriteVehicleId(Integer ownerId, Integer favoriteId);
}