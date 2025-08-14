package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.GarageFavorite;
import com.autoadda.apis.garage.repository.GarageFavoriteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GarageFavoriteService {

	private final GarageFavoriteRepository garageFavRepo;

	public GarageFavorite addFavoriteGarage(Integer garageOwnerId, Integer favGarageId) {
		if (garageFavRepo.existsByGarageOwnerIdAndFavoriteGarageId(garageOwnerId, favGarageId)) {
			throw new RuntimeException("Already marked as favorite");
		}
		GarageFavorite fav = new GarageFavorite();
		fav.setGarageOwnerId(garageOwnerId);
		fav.setFavoriteGarageId(favGarageId);
		fav.setAddedAt(LocalDateTime.now());
		return garageFavRepo.save(fav);
	}

	public List<GarageFavorite> getFavoriteGarages(Integer garageOwnerId) {
		return garageFavRepo.findByGarageOwnerId(garageOwnerId);
	}

	public void removeFavoriteGarage(Integer garageOwnerId, Integer favGarageId) {
		garageFavRepo.findAll().stream().filter(
				fav -> fav.getGarageOwnerId().equals(garageOwnerId) && fav.getFavoriteGarageId().equals(favGarageId))
				.findFirst().ifPresent(garageFavRepo::delete);
	}

}
