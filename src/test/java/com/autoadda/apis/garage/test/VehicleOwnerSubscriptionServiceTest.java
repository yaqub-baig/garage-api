package com.autoadda.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.repository.GarageSubscriptionRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerSubscriptionRepository;
import com.autoadda.apis.garage.service.VehicleOwnerSubscriptionService;
import com.autoadda.apis.garage.util.SubscriptionStatus;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
class VehicleOwnerSubscriptionServiceTest {

	@InjectMocks
	private VehicleOwnerSubscriptionService subscriptionService;

	@Mock
	private VehicleOwnerRepository vehicleOwnerRepository;

	@Mock
	private GarageSubscriptionRepository planRepository;

	@Mock
	private VehicleOwnerSubscriptionRepository subscriptionRepository;

	private VehicleOwner owner;
	private GarageSubscription plan;
	private VehicleOwnerSubscription subscription;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		owner = new VehicleOwner();
		owner.setId(1);
		owner.setPhone("8479098528");

		plan = new GarageSubscription();
		plan.setId(101);
		plan.setName("Premium Wash");
		plan.setDurationMonths(3);

		subscription = new VehicleOwnerSubscription();
		subscription.setId(1001);
		subscription.setVehicleOwner(owner);
		subscription.setGarageSubscription(plan);
		subscription.setStartDate(LocalDate.now());
		subscription.setEndDate(LocalDate.now().plusMonths(3));
		subscription.setStatus(SubscriptionStatus.ACTIVE);
	}

	@Test
	void testSubscribeToPlan_Success() {
		when(vehicleOwnerRepository.findById(1)).thenReturn(Optional.of(owner));
		when(planRepository.findById(101)).thenReturn(Optional.of(plan));
		when(subscriptionRepository.save(any())).thenReturn(subscription);

		VehicleOwnerSubscription result = subscriptionService.subscribeToPlan(subscription);

		assertNotNull(result);
		assertEquals(owner, result.getVehicleOwner());
		assertEquals(plan, result.getGarageSubscription());
		assertEquals(SubscriptionStatus.ACTIVE, result.getStatus());
	}

	@Test
	void testSubscribeToPlan_OwnerNotFound() {
		when(vehicleOwnerRepository.findById(1)).thenReturn(Optional.empty());

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> subscriptionService.subscribeToPlan(subscription));

		assertEquals("Vehicle owner not found", ex.getMessage());
	}

	@Test
	void testSubscribeToPlan_PlanNotFound() {
		when(vehicleOwnerRepository.findById(1)).thenReturn(Optional.of(owner));
		when(planRepository.findById(101)).thenReturn(Optional.empty());

		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
				() -> subscriptionService.subscribeToPlan(subscription));

		assertEquals("Subscription plan not found", ex.getMessage());
	}

	@Test
	void testGetSubscriptionsByOwner() {
		List<VehicleOwnerSubscription> list = List.of(subscription);
		when(subscriptionRepository.findByVehicleOwnerId(1)).thenReturn(list);

		List<VehicleOwnerSubscription> result = subscriptionService.getSubscriptionsByOwner(1);
		assertEquals(1, result.size());
	}

	@Test
	void testCancelSubscription() {
		when(subscriptionRepository.findById(1001)).thenReturn(Optional.of(subscription));
		subscriptionService.cancelSubscription(1001);
		assertEquals(SubscriptionStatus.CANCELLED, subscription.getStatus());
		verify(subscriptionRepository).save(subscription);
	}

	@Test
	void testGetSubscriptionById_Success() {
		when(subscriptionRepository.findById(1001)).thenReturn(Optional.of(subscription));
		VehicleOwnerSubscription result = subscriptionService.getSubscriptionById(1001);
		assertNotNull(result);
	}

	@Test
	void testGetSubscriptionById_NotFound() {
		when(subscriptionRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> subscriptionService.getSubscriptionById(999));
	}

	@Test
	void testGetActiveSubscriptions() {
		when(subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(subscription));
		List<VehicleOwnerSubscription> result = subscriptionService.getActiveSubscriptions();
		assertEquals(1, result.size());
	}

	@Test
	void testGetSubscriptionsByDateRange() {
		LocalDate start = LocalDate.now().minusDays(1);
		LocalDate end = LocalDate.now().plusDays(1);

		when(subscriptionRepository.findByVehicleOwnerIdAndStartDateBetween(1, start, end))
				.thenReturn(List.of(subscription));

		List<VehicleOwnerSubscription> result = subscriptionService.getSubscriptionsByDateRange(1, start, end);
		assertEquals(1, result.size());
	}

	@Test
	void testGetSubscriptionsByPlan() {
		when(subscriptionRepository.findByGarageSubscriptionId(101)).thenReturn(List.of(subscription));
		List<VehicleOwnerSubscription> result = subscriptionService.getSubscriptionsByPlan(101);
		assertEquals(1, result.size());
	}

	@Test
	void testGetSubscriptionsByOwnerAndPlan() {
		when(subscriptionRepository.findByVehicleOwnerIdAndGarageSubscriptionId(1, 101))
				.thenReturn(List.of(subscription));

		List<VehicleOwnerSubscription> result = subscriptionService.getSubscriptionsByOwnerAndPlan(1, 101);
		assertEquals(1, result.size());
	}

	@Test
	void testGetSubscriptionsByOwnerAndStatus() {
		when(subscriptionRepository.findByVehicleOwnerIdAndStatus(1, SubscriptionStatus.ACTIVE.name()))
				.thenReturn(List.of(subscription));

		List<VehicleOwnerSubscription> result = subscriptionService.getSubscriptionsByOwnerAndStatus(1,
				SubscriptionStatus.ACTIVE.name());
		assertEquals(1, result.size());
	}

	@Test
	void testGetSubscriptionsByPlanAndStatus() {
		when(subscriptionRepository.findByGarageSubscriptionIdAndStatus(101, SubscriptionStatus.ACTIVE.name()))
				.thenReturn(List.of(subscription));

		List<VehicleOwnerSubscription> result = subscriptionService.getSubscriptionsByPlanAndStatus(101,
				SubscriptionStatus.ACTIVE.name());
		assertEquals(1, result.size());
	}

	@Test
	void testUpdateSubscriptionStatus() {
		when(subscriptionRepository.findById(1001)).thenReturn(Optional.of(subscription));
		when(subscriptionRepository.save(subscription)).thenReturn(subscription);

		VehicleOwnerSubscription updated = subscriptionService.updateSubscriptionStatus(1001,
				SubscriptionStatus.EXPIRED);

		assertEquals(SubscriptionStatus.EXPIRED, updated.getStatus());
	}
}
