package com.autoadda.apis.garage.test;

//✅ Import JUnit 5 assertions
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.util.Status;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class GarageServiceTest {

	@InjectMocks
	private GarageManagementService garageService;

	@Mock
	private GarageRepository garageRepository;

	private GarageRequest sampleGarageRequest;
	private Garage sampleGarageEntity;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		sampleGarageRequest = new GarageRequest();
		sampleGarageRequest.setGarageName("QuickFix");
		sampleGarageRequest.setLegalName("QuickFix LLC");
		sampleGarageRequest.setEmailId("info@quickfix.com");
		sampleGarageRequest.setContactNumber("1234567890");
		sampleGarageRequest.setWebSite("http://quickfix.com");
		sampleGarageRequest.setAccountStatus(Status.ACTIVE);
		sampleGarageRequest.setGarageType("Multi-Brand");
		sampleGarageRequest.setChainId(101);
		sampleGarageRequest.setCreatedAt(LocalDate.now());

		sampleGarageEntity = new Garage();
		sampleGarageEntity.setId(1);
		sampleGarageEntity.setGarageName("QuickFix");
		sampleGarageEntity.setLegalName("QuickFix LLC");
		sampleGarageEntity.setEmailId("info@quickfix.com");
		sampleGarageEntity.setContactNumber("1234567890");
		sampleGarageEntity.setWebSite("http://quickfix.com");
		sampleGarageEntity.setAccountStatus(Status.ACTIVE);
		sampleGarageEntity.setGarageType("Multi-Brand");
		sampleGarageEntity.setChainId(101);
	}

	@Test
	void testCreateGarage() {
		when(garageRepository.save(any(Garage.class))).thenReturn(sampleGarageEntity);
		Garage result = garageService.createGarage(sampleGarageRequest);
		assertNotNull(result);
		assertEquals("QuickFix", result.getGarageName());
		assertEquals("QuickFix LLC", result.getLegalName());
	}

	@Test
	void testGetAllGarages() {
		when(garageRepository.findAll()).thenReturn(List.of(sampleGarageEntity));
		List<Garage> garages = garageService.getAllGarages();
		assertEquals(1, garages.size());
		assertEquals("QuickFix", garages.get(0).getGarageName());
	}

	@Test
	void testGetGarageByIdSuccess() {
		when(garageRepository.findById(1)).thenReturn(Optional.of(sampleGarageEntity));
		Optional<Garage> result = garageService.getGarageById(1);
		assertTrue(result.isPresent());
		assertEquals("QuickFix", result.get().getGarageName());
	}

	@Test
	void testUpdateGarageSuccess() {
		when(garageRepository.findById(1)).thenReturn(Optional.of(sampleGarageEntity));
		when(garageRepository.save(any(Garage.class))).thenReturn(sampleGarageEntity);

		GarageRequest updatedRequest = new GarageRequest();
		updatedRequest.setGarageName("SpeedFix");
		updatedRequest.setLegalName("SpeedFix Inc");
		updatedRequest.setContactNumber("9998887777");

		Garage updatedGarage = garageService.updateGarage(1, updatedRequest);

		assertNotNull(updatedGarage);
		assertEquals("SpeedFix", updatedGarage.getGarageName());
		assertEquals("SpeedFix Inc", updatedGarage.getLegalName());
		assertEquals("9998887777", updatedGarage.getContactNumber());
	}

	@Test
	void testSoftDeleteGarage() {
		when(garageRepository.findById(1)).thenReturn(Optional.of(sampleGarageEntity));
		sampleGarageEntity.setAccountStatus(Status.REMOVED);
		when(garageRepository.save(any(Garage.class))).thenReturn(sampleGarageEntity);

		garageService.deleteGarage(1);
		assertEquals(Status.REMOVED, sampleGarageEntity.getAccountStatus());
	}
}
