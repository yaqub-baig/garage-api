package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.garage.apis.entity.GarageFavorite;

public interface GarageFavoriteRepository extends JpaRepository<GarageFavorite, Integer> {
	List<GarageFavorite> findByGarageOwnerId(Integer garageOwnerId);

	boolean existsByGarageOwnerIdAndFavoriteGarageId(Integer ownerId, Integer favoriteId);
}