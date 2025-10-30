package com.autoadda.apis.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.autoadda.apis.garage.request.SubscriptionRequest;
import com.autoadda.apis.garage.response.VehicleOwnerSubscriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerSubscriptionRepository;
import com.autoadda.apis.garage.service.VehicleOwnerSubscriptionService;
import com.autoadda.apis.garage.util.SubscriptionStatus;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
class VehicleOwnerSubscriptionServiceTest {

	@Mock
	private VehicleOwnerSubscriptionRepository vehicleOwnerSubscriptionRepository;

	@Mock
	private VehicleOwnerRepository vehicleOwnerRepository;

	@Mock
	private GarageSubscriptionRepository garageSubscriptionRepository;

	@InjectMocks
	private VehicleOwnerSubscriptionService vehicleOwnerSubscriptionService;

	private VehicleOwner owner;
	private GarageSubscription plan;
	private VehicleOwnerSubscription subscription;
	private SubscriptionRequest subscriptionRequest;
	@BeforeEach
	void setUp() {

		subscriptionRequest=new SubscriptionRequest();
		owner = new VehicleOwner();
		owner.setId(12);

		plan = new GarageSubscription();
		plan.setId(123);

		subscription = new VehicleOwnerSubscription();
		subscription.setId(1001);
		subscription.setCustomerId (owner.getId ());
		subscription.setGarageSubscription(plan);
		subscription.setStartDate(LocalDate.now());
		subscription.setEndDate(LocalDate.now().plusMonths(3));
		subscription.setStatus(SubscriptionStatus.ACTIVE);
	}

	@Test
	void testGetSubscriptionById() {
		// Arrange (mock behavior)
		when(vehicleOwnerSubscriptionRepository.findById(1001)).thenReturn(Optional.of(subscription));

		// Act
		VehicleOwnerSubscription result = vehicleOwnerSubscriptionService.getSubscriptionById(1001);

		// Assert
		assertNotNull(result);
		assertEquals(12, result.getCustomerId ());
		assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());

		// Verify that repo method was called once
		verify(vehicleOwnerSubscriptionRepository, times(1)).findById(1001);
	}

	@Test
	void testGetSubscriptionById_NotFound() {
		// Arrange
		when(vehicleOwnerSubscriptionRepository.findById(9999)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(EntityNotFoundException.class, () -> {
			vehicleOwnerSubscriptionService.getSubscriptionById(9999);
		});

		verify(vehicleOwnerSubscriptionRepository, times(1)).findById(9999);
	}

	@Test
	void testSubscribeToPlan() {
		VehicleOwner owner = new VehicleOwner();
		owner.setId(12);

		GarageSubscription plan = new GarageSubscription();
		plan.setId(123);
		plan.setDurationMonths(6);

		VehicleOwnerSubscription subscription = new VehicleOwnerSubscription();
		subscription.setCustomerId (owner.getId ());
		subscription.setGarageSubscription(plan);

		when(vehicleOwnerRepository.findById(12)).thenReturn(Optional.of(owner));

		when(garageSubscriptionRepository.findById(123)).thenReturn(Optional.of(plan));

		when(vehicleOwnerSubscriptionRepository.save(any(VehicleOwnerSubscription.class))).thenReturn(subscription);

		VehicleOwnerSubscription result = vehicleOwnerSubscriptionService.subscribeToPlan(subscriptionRequest);

		assertNotNull(result);
		assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
		assertEquals(LocalDate.now(), result.getStartDate());
		assertEquals(LocalDate.now().plusMonths(6), result.getEndDate());

		verify(vehicleOwnerRepository, times(1)).findById(12);
		verify(garageSubscriptionRepository, times(1)).findById(123);
		verify(vehicleOwnerSubscriptionRepository, times(1)).save(subscription);
	}

	@Test
	void testCancelSubscription() {
		when(vehicleOwnerSubscriptionRepository.findById(1001)).thenReturn(Optional.of(subscription));

		vehicleOwnerSubscriptionService.cancelSubscription(1001);

		assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
		verify(vehicleOwnerSubscriptionRepository, times(1)).save(subscription);
	}

	@Test
	void testCancelSubscription_NotFound() {
		when(vehicleOwnerSubscriptionRepository.findById(9999)).thenReturn(Optional.empty());
		assertThrows(EntityNotFoundException.class, () -> {
			vehicleOwnerSubscriptionService.cancelSubscription(9999);
		});
		verify(vehicleOwnerSubscriptionRepository, times(1)).findById(9999);
	}

	@Test
	void testGetSubscriptionsByOwner() {
		when(vehicleOwnerSubscriptionRepository.findByCustomerId (12)).thenReturn(List.of(subscription));

		List<VehicleOwnerSubscriptionResponse> subscriptions = vehicleOwnerSubscriptionService.getSubscriptionsByOwner(12);
		assertEquals(1, subscriptions.size());
		assertEquals(1001, subscriptions.get(0).getId());

		verify(vehicleOwnerSubscriptionRepository, times(1)).findByCustomerId (12);
	}

	@Test
	void testGetAllActiveSubscriptions() {
		when(vehicleOwnerSubscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE))
				.thenReturn(List.of(subscription));

		List<VehicleOwnerSubscriptionResponse> activeSubscriptions = vehicleOwnerSubscriptionService.getActiveSubscriptions();
		assertEquals(1, activeSubscriptions.size());
		assertEquals(1001, activeSubscriptions.get(0).getId());

		verify(vehicleOwnerSubscriptionRepository, times(1)).findByStatus(SubscriptionStatus.ACTIVE);
	}

	@Test
	void testGetAllSubscriptions() {
		when(vehicleOwnerSubscriptionRepository.findAll()).thenReturn(List.of(subscription));

		List<VehicleOwnerSubscription> allSubscriptions = vehicleOwnerSubscriptionService.getAllSubscriptions();
		System.out.println("Returned size: " + allSubscriptions.size());
		System.out.println("Returned list: " + allSubscriptions);

		assertEquals(1, allSubscriptions.size());
		assertEquals(1001, allSubscriptions.get(0).getId());

		verify(vehicleOwnerSubscriptionRepository, times(1)).findAll();
	}
}
