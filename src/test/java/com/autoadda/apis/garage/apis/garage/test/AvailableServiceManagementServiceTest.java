package com.autoadda.apis.garage.apis.garage.test;

// ✅ Import JUnit 5 assertions
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
// ✅ Import Mockito methods
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.request.AvailableServiceRequest;
import com.autoadda.apis.garage.service.AvailableServiceManagementService;

@ExtendWith(MockitoExtension.class)
class AvailableServiceManagementServiceTest {

	@InjectMocks
	private AvailableServiceManagementService service;

	@Mock
	private AvailableServiceRepository repository;

	private AvailableService sampleService;
	private AvailableServiceRequest sampleRequest;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		sampleService = AvailableService.builder().id(1).name("Oil Change").description("Engine oil replacement")
				.price(50.0).laborPrice(20.0).build();

		sampleRequest = AvailableServiceRequest.builder().name("Oil Change").description("Engine oil replacement")
				.price(50.0).laborPrice(20.0).build();
	}

	@Test
	void testCreateService_success() {
		AvailableServiceRequest request = new AvailableServiceRequest();
		request.setName("Test");
		request.setDescription("Desc");
		request.setPrice(100.0);
		request.setLaborPrice(20.0);

		AvailableService expected = AvailableService.builder().name("Test").description("Desc").price(100.0)
				.laborPrice(20.0).build();

		when(repository.save(any(AvailableService.class))).thenReturn(expected);

		AvailableService result = service.createService(request);

		assertEquals("Test", result.getName());
		assertEquals("Desc", result.getDescription());
	}

	@Test
	void testGetAllServices_success() {
		List<AvailableService> services = List.of(sampleService);
		when(repository.findAll()).thenReturn(services);

		List<AvailableService> result = service.getAllServices();

		assertEquals(1, result.size());
		assertEquals("Oil Change", result.get(0).getName());
		verify(repository).findAll();
	}

	@Test
	void testGetServiceById_found() {
		when(repository.findById(1)).thenReturn(Optional.of(sampleService));

		Optional<AvailableService> result = service.getServiceById(1);

		assertTrue(result.isPresent());
		assertEquals("Oil Change", result.get().getName());
	}

	@Test
	void testGetServiceById_notFound() {
		when(repository.findById(2)).thenReturn(Optional.empty());

		Optional<AvailableService> result = service.getServiceById(2);

		assertFalse(result.isPresent());
	}

	@Test
	void testUpdateService_success() {
		AvailableService updatedData = AvailableService.builder().name("Brake Service")
				.description("Brake pad replacement").price(100.0).laborPrice(30.0).build();

		when(repository.findById(1)).thenReturn(Optional.of(sampleService));
		when(repository.save(any(AvailableService.class))).thenReturn(updatedData);

		AvailableService result = service.updateService(1, updatedData);

		assertEquals("Brake Service", result.getName());
		assertEquals("Brake pad replacement", result.getDescription());
	}

	@Test
	void testUpdateService_notFound() {
		when(repository.findById(99)).thenReturn(Optional.empty());

		RuntimeException ex = assertThrows(RuntimeException.class, () -> {
			service.updateService(99, sampleService);
		});

		assertEquals("Service not found with ID: 99", ex.getMessage());
	}

	@Test
	void testDeleteService_success() {
		doNothing().when(repository).deleteById(1);

		service.deleteService(1);

		verify(repository).deleteById(1);
	}

	@Test
	void testSearchByName_success() {
		when(repository.findByNameContainingIgnoreCase("oil")).thenReturn(List.of(sampleService));

		List<AvailableService> results = service.searchByName("oil");

		assertEquals(1, results.size());
		assertEquals("Oil Change", results.get(0).getName());
	}

	@Test
	 void testSearchByDescription_success() {
		String keyword = "oil change";

		AvailableService srvc = new AvailableService();
		srvc.setId(1);
		srvc.setDescription("Oil Change");
		srvc.setPrice(100.0);

		List<AvailableService> mockList = List.of(srvc);

		when(repository.findByDescriptionContainingIgnoreCase(keyword)).thenReturn(mockList);

		List<AvailableService> results = service.searchByDescription(keyword);

		assertNotNull(results); // <-- Make sure this line uses assertNotNull (not assetNull)
		assertFalse(results.isEmpty());
		assertTrue(results.stream().anyMatch(s -> s.getDescription().equalsIgnoreCase("oil change")));
	}

	@Test
	void testSearchByPriceRange_success() {
		when(repository.findByPriceBetween(30.0, 60.0)).thenReturn(List.of(sampleService));

		List<AvailableService> results = service.searchByPriceRange(30.0, 60.0);

		assertEquals(1, results.size());
		assertEquals(50.0, results.get(0).getPrice());
	}
}
