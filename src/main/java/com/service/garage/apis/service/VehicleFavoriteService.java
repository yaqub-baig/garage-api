package com.service.garage.apis.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.VehicleFavorite;
import com.service.garage.apis.repository.VehicleFavoriteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleFavoriteService {

	private final VehicleFavoriteRepository vehicleFavRepo;

	public VehicleFavorite addFavoriteVehicle(Integer vehicleOwnerId, Integer favVehicleId) {
		if (vehicleFavRepo.existsByVehicleOwnerIdAndFavoriteVehicleId(vehicleOwnerId, favVehicleId)) {
			throw new RuntimeException("Already marked as favorite");
		}
		VehicleFavorite fav = new VehicleFavorite();
		fav.setVehicleOwnerId(vehicleOwnerId);
		fav.setFavoriteVehicleId(favVehicleId);
		fav.setAddedAt(LocalDateTime.now());
		return vehicleFavRepo.save(fav);
	}

	public List<VehicleFavorite> getFavoriteVehicles(Integer vehicleOwnerId) {
		return vehicleFavRepo.findByVehicleOwnerId(vehicleOwnerId);
	}

	public void removeFavoriteVehicle(Integer vehicleOwnerId, Integer favVehicleId) {
		vehicleFavRepo.findAll().stream()
				.filter(fav -> fav.getVehicleOwnerId().equals(vehicleOwnerId)
						&& fav.getFavoriteVehicleId().equals(favVehicleId))
				.findFirst().ifPresent(vehicleFavRepo::delete);
	}
}
