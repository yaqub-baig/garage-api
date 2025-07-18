package com.autoadda.apis.garage.test;

// <-- REQUIRED
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.util.Status;

@ExtendWith(MockitoExtension.class)
public class GarageManagementServiceTest {

	@Mock
	private GarageRepository garageRepository;

	@InjectMocks
	private GarageManagementService garageService;

	private Garage sampleGarage;

	@BeforeEach
	void setup() {
		sampleGarage = new Garage();
		sampleGarage.setId(1);
		sampleGarage.setGarageName("QuickFix");
		sampleGarage.setLegalName("QuickFix LLC");
		sampleGarage.setEmailId("info@quickfix.com");
		sampleGarage.setContactNumber("1234567890");
		sampleGarage.setWebSite("http://quickfix.com");
		sampleGarage.setAccountStatus(Status.ACTIVE);
		sampleGarage.setGarageType("Multi-Brand");
		sampleGarage.setChainId(101);
		sampleGarage.setInsertTms(LocalDate.now());
	}

	@Test
	void testCreateGarage() {
		when(garageRepository.save(any(Garage.class))).thenReturn(sampleGarage);
		Garage result = garageService.createGarage(sampleGarage);
		assertEquals(sampleGarage.getGarageName(), result.getGarageName());
	}

	@Test
	void testGetAllGarages() {
		when(garageRepository.findAll()).thenReturn(List.of(sampleGarage));
		List<Garage> garages = garageService.getAllGarages();
		assertEquals(1, garages.size());
	}

	@Test
	void testGetGarageByIdSuccess() {
		when(garageRepository.findById(1)).thenReturn(Optional.of(sampleGarage));
		Optional<Garage> result = garageService.getGarageById(1);
		assertTrue(result.isPresent());
	}

	@Test
	void testGetGarageByIdFailure() {
		when(garageRepository.findById(99)).thenReturn(Optional.empty());
		assertThrows(NoSuchElementException.class, () -> garageService.getGarageById(99));
	}

	@Test
	void testUpdateGarageSuccess() {
		Garage updated = new Garage();
		updated.setGarageName("NewName");
		when(garageRepository.findById(1)).thenReturn(Optional.of(sampleGarage));
		when(garageRepository.save(any())).thenReturn(sampleGarage);

		Garage result = garageService.updateGarage(1, updated);
		assertEquals("NewName", result.getGarageName());
	}

	@Test
	void testDeleteGarage() {
		doNothing().when(garageRepository).deleteById(1);
		garageService.deleteGarage(1);
		verify(garageRepository, times(1)).deleteById(1);
	}

	@Test
	void testFindByGarageNameContainingIgnoreCase() {
		when(garageRepository.findByGarageNameContainingIgnoreCase("quick")).thenReturn(List.of(sampleGarage));
		List<Garage> result = garageService.findGaragesByGarageName("quick");
		assertEquals(1, result.size());
	}

	@Test
	void testFindGarageByEmail() {
		when(garageRepository.findByEmailId("info@quickfix.com")).thenReturn(Optional.of(sampleGarage));
		Optional<Garage> result = garageService.findGarageByEmail("info@quickfix.com");
		assertTrue(result.isPresent());
	}

	@Test
	void testFindGarageByIdAndStatus() {
		when(garageRepository.findByIdAndAccountStatus(1, Status.ACTIVE)).thenReturn(Optional.of(sampleGarage));
		Optional<Garage> result = garageService.findGarageByIdAndStatus(1, Status.ACTIVE);
		assertTrue(result.isPresent());
	}

	@Test
	void testFindGarageByIdStatusChain() {
		when(garageRepository.findByIdAndAccountStatusAndChainId(1, Status.ACTIVE, 101))
				.thenReturn(Optional.of(sampleGarage));
		Optional<Garage> result = garageService.findGarageByIdStatusChain(1, Status.ACTIVE, 101);
		assertTrue(result.isPresent());
	}
}
