package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.GarageFavorite;

public interface GarageFavoriteRepository extends JpaRepository<GarageFavorite, Integer> {
	List<GarageFavorite> findByVehicleOwnerId(Integer garageOwnerId);

	boolean existsByVehicleOwnerIdAndFavoriteGarageId(Integer ownerId, Integer favoriteId);
}