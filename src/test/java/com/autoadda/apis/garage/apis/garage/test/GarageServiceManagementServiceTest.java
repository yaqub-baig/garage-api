package com.autoadda.apis.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.autoadda.apis.garage.request.GarageServiceRequest;
import com.autoadda.apis.garage.response.GarageServiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.repository.GarageServiceRepository;
import com.autoadda.apis.garage.service.GarageServiceManagementService;

@ExtendWith(MockitoExtension.class)
class GarageServiceManagementServiceTest {

	@Mock
	private GarageServiceRepository garageServiceRepository;

	@InjectMocks
	private GarageServiceManagementService garageServiceManagementService;

	private Garage garage;
	private AvailableService availableService;
	private GarageService garageService;
	private GarageServiceRequest garageServiceRequest;
	@BeforeEach
	void setUp() {
		garage = Garage.builder().id(1).name ("My Garage").build();
		availableService = AvailableService.builder().id(1).name("Oil Change").build();
		garageService = GarageService.builder().id(1).garage(garage).availableService(availableService).isActive(true)
				.build();
	}

	@Test
	void testCreateGarageService() {
		when(garageServiceRepository.save(garageService)).thenReturn(garageService);
		GarageService saved = garageServiceManagementService.createGarageService(garageServiceRequest);
		assertEquals(garageService, saved);
		verify(garageServiceRepository).save(garageService);
	}

	@Test
	void testGetAllGarageServices() {
		when(garageServiceRepository.findAll()).thenReturn(List.of(garageService));
		List<GarageService> result = garageServiceManagementService.getAllGarageServices();
		assertEquals(1, result.size());
		assertEquals(garageService, result.get(0));
	}

	@Test
	void testGetGarageServiceByIdFound() {
		when(garageServiceRepository.findById(1)).thenReturn(Optional.of(garageService));
		Optional<GarageService> result = garageServiceManagementService.getGarageServiceById(1);
		assertTrue(result.isPresent());
		assertEquals(garageService, result.get());
	}

	@Test
	void testGetGarageServiceByIdNotFound() {
		when(garageServiceRepository.findById(99)).thenReturn(Optional.empty());
		Optional<GarageService> result = garageServiceManagementService.getGarageServiceById(99);
		assertFalse(result.isPresent());
	}

	@Test
	void testUpdateGarageServiceSuccess() {
		GarageService updated = GarageService.builder().isActive(false).garage(garage)
				.availableService(availableService).build();

		when(garageServiceRepository.findById(1)).thenReturn(Optional.of(garageService));
		when(garageServiceRepository.save(any())).thenReturn(updated);

		GarageService result = garageServiceManagementService.updateGarageService(1, updated);

		assertFalse(result.getIsActive());
		assertEquals(garage, result.getGarage());
		assertEquals(availableService, result.getAvailableService());
	}

	@Test
	void testDeleteGarageService() {
		garageServiceManagementService.deleteGarageService(1);
		verify(garageServiceRepository).deleteById(1);
	}

	@Test
	void testUpdateGarageServiceNotFound() {
		when(garageServiceRepository.findById(999)).thenReturn(Optional.empty());

		GarageService updated = GarageService.builder().isActive(false).build();

		assertThrows(RuntimeException.class, () -> garageServiceManagementService.updateGarageService(999, updated));
	}

	@Test
	void testGetByGarageId() {
		when(garageServiceRepository.findByGarage_Id(1)).thenReturn(List.of(garageService));
		List<GarageServiceResponse> result = garageServiceManagementService.getByGarageId(1);
		assertEquals(1, result.size());
	}

	@Test
	void testGetByActiveStatus() {
		when(garageServiceRepository.findByIsActive(true)).thenReturn(List.of(garageService));
		List<GarageService> result = garageServiceManagementService.getByActiveStatus(true);
		assertEquals(1, result.size());
		assertTrue(result.get(0).getIsActive());
	}

	@Test
	void testGetByGarageIdAndActive() {
		when(garageServiceRepository.findByGarage_IdAndIsActive(1, true)).thenReturn(List.of(garageService));
		List<GarageService> result = garageServiceManagementService.getByGarageIdAndActive(1, true);
		assertEquals(1, result.size());
	}

	@Test
	void testGetByServiceId() {
		when(garageServiceRepository.findByAvailableService_Id(1)).thenReturn(List.of(garageService));
		List<GarageService> result = garageServiceManagementService.getByServiceId(1);
		assertEquals(1, result.size());
	}

	@Test
	void testGetByGarageAndServiceId() {
		when(garageServiceRepository.findByGarage_IdAndAvailableService_Id(1, 1)).thenReturn(List.of(garageService));
		List<GarageService> result = garageServiceManagementService.getByGarageAndServiceId(1, 1);
		assertEquals(1, result.size());
	}
}
