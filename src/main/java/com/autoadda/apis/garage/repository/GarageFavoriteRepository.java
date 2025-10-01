package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarageFavoriteRepository extends JpaRepository<GarageFavorite, Integer> {
    List<GarageFavorite> findByVehicleOwnerId(Integer garageOwnerId);

    boolean existsByVehicleOwnerIdAndFavoriteGarageId(Integer ownerId, Integer favoriteId);
}