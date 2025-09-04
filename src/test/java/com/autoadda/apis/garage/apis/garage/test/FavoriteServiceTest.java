package com.autoadda.apis.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.garage.entity.GarageFavorite;
import com.autoadda.apis.garage.entity.VehicleFavorite;
import com.autoadda.apis.garage.repository.GarageFavoriteRepository;
import com.autoadda.apis.garage.repository.VehicleFavoriteRepository;
import com.autoadda.apis.garage.service.GarageFavoriteService;
import com.autoadda.apis.garage.service.VehicleFavoriteService;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

	@InjectMocks
	private VehicleFavoriteService vehicleFavoriteService;

	@InjectMocks
	private GarageFavoriteService garagefavoriteService;

	@Mock
	private GarageFavoriteRepository garageFavoriteRepository;

	@Mock
	private VehicleFavoriteRepository vehicleFavoriteRepository;

	// --- Test Data Setup ---
	private final Integer garageOwnerId = 101;
	private final Integer favGarageId = 202;
	private final Integer vehicleOwnerId = 301;
	private final Integer favVehicleId = 401;

	// --- Tests for addFavoriteGarage ---
	@Test
	void addFavoriteGarage_success() {
		when(garageFavoriteRepository.existsByVehicleOwnerIdAndFavoriteGarageId (garageOwnerId, favGarageId))
				.thenReturn(false);
		when(garageFavoriteRepository.save(any(GarageFavorite.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		GarageFavorite result = garagefavoriteService.addFavoriteGarage(garageOwnerId, favGarageId);

		assertEquals(garageOwnerId, result.getVehicleOwnerId ());

		assertNotNull(result.getAddedAt());
	}

	@Test
	void addFavoriteGarage_alreadyExists_throwsException() {
		when(garageFavoriteRepository.existsByVehicleOwnerIdAndFavoriteGarageId (garageOwnerId, favGarageId))
				.thenReturn(true);

		assertThrows(RuntimeException.class, () -> garagefavoriteService.addFavoriteGarage(garageOwnerId, favGarageId));
	}

	// --- Tests for addFavoriteVehicle ---
	@Test
	void addFavoriteVehicle_success() {
		when(vehicleFavoriteRepository.existsByVehicleOwnerIdAndFavoriteVehicleId(vehicleOwnerId, favVehicleId))
				.thenReturn(false);
		when(vehicleFavoriteRepository.save(any(VehicleFavorite.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		VehicleFavorite result = vehicleFavoriteService.addFavoriteVehicle(vehicleOwnerId, favVehicleId);

		assertEquals(vehicleOwnerId, result.getVehicleOwnerId());
		assertEquals(favVehicleId, result.getFavoriteVehicleId());
		assertNotNull(result.getAddedAt());
	}

	@Test
	void addFavoriteVehicle_alreadyExists_throwsException() {
		when(vehicleFavoriteRepository.existsByVehicleOwnerIdAndFavoriteVehicleId(vehicleOwnerId, favVehicleId))
				.thenReturn(true);

		assertThrows(RuntimeException.class,
				() -> vehicleFavoriteService.addFavoriteVehicle(vehicleOwnerId, favVehicleId));
	}

	// --- Tests for getFavoriteGarages ---
	@Test
	void getFavoriteGarages_returnsList() {
		List<GarageFavorite> mockList = List.of(new GarageFavorite(), new GarageFavorite());
		when(garageFavoriteRepository.findByVehicleOwnerId (garageOwnerId)).thenReturn(mockList);

		List<GarageFavorite> result = garagefavoriteService.getFavoriteGarages(garageOwnerId);

		assertEquals(2, result.size());
	}

	// --- Tests for getFavoriteVehicles ---
	@Test
	void getFavoriteVehicles_returnsList() {
		List<VehicleFavorite> mockList = List.of(new VehicleFavorite());
		when(vehicleFavoriteRepository.findByVehicleOwnerId(vehicleOwnerId)).thenReturn(mockList);

		List<VehicleFavorite> result = vehicleFavoriteService.getFavoriteVehicles(vehicleOwnerId);

		assertEquals(1, result.size());
	}

	// --- Tests for removeFavoriteGarage ---
	@Test
	void removeFavoriteGarage_deletesIfExists() {
		GarageFavorite fav = new GarageFavorite();
		fav.setVehicleOwnerId (garageOwnerId);
		when(garageFavoriteRepository.findAll()).thenReturn(List.of(fav));

		garagefavoriteService.removeFavoriteGarage(garageOwnerId, favGarageId);

		verify(garageFavoriteRepository, times(1)).delete(fav);
	}

	// --- Tests for removeFavoriteVehicle ---
	@Test
	void removeFavoriteVehicle_deletesIfExists() {
		VehicleFavorite fav = new VehicleFavorite();
		fav.setVehicleOwnerId(vehicleOwnerId);
		fav.setFavoriteVehicleId(favVehicleId);
		when(vehicleFavoriteRepository.findAll()).thenReturn(List.of(fav));

		vehicleFavoriteService.removeFavoriteVehicle(vehicleOwnerId, favVehicleId);

		verify(vehicleFavoriteRepository, times(1)).delete(fav);
	}
}
