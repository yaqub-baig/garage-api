package com.autoadda.apis.garage.apis.garage.test;

import com.autoadda.apis.garage.entity.CommunicationPreference;
import com.autoadda.apis.garage.repository.CommunicationPreferenceRepository;
import com.autoadda.apis.garage.request.CommunicationPreferenceRequest;
import com.autoadda.apis.garage.service.CommunicationPreferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class CommunicationPreferenceServiceTest {

	@InjectMocks
	private CommunicationPreferenceService service;

	@Mock
	private CommunicationPreferenceRepository repository;

	@Test
	 void testSaveNewPreference() {
		CommunicationPreferenceRequest pref = new CommunicationPreferenceRequest();
		pref.setCustomerId(1);
		pref.setEmailOnServiceUpdates(true);

		when(repository.findByCustomerId(1)).thenReturn(Optional.empty());
		when(repository.save(any(CommunicationPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CommunicationPreference saved = service.saveOrUpdatePreference(pref);

		assertNotNull(saved);
		assertTrue(saved.getEmailOnServiceUpdates());
	}

	private CommunicationPreference mapToEntity(CommunicationPreferenceRequest preferenceRequest) {
		return CommunicationPreference.builder().id(preferenceRequest.getId())
				.customerId(preferenceRequest.getCustomerId())
				.emailOnServiceUpdates(preferenceRequest.getEmailOnServiceUpdates())
				.smsOnServiceUpdates(preferenceRequest.getSmsOnServiceUpdates())
				.emailOnAppointments(preferenceRequest.getEmailOnAppointments())
				.smsOnAppointments(preferenceRequest.getSmsOnAppointments())
				.emailOnReminders(preferenceRequest.getEmailOnReminders())
				.smsOnReminders(preferenceRequest.getSmsOnReminders())
				.emailOnOffers(preferenceRequest.getEmailOnOffers()).smsOnOffers(preferenceRequest.getSmsOnOffers())
				.emailOnQuotationRequests(preferenceRequest.getEmailOnQuotationRequests())
				.smsOnQuotationRequests(preferenceRequest.getSmsOnQuotationRequests())
				.status(preferenceRequest.getStatus()).build();
	}

	@Test
	 void testGetPreference() {
		CommunicationPreference pref = new CommunicationPreference();
		pref.setCustomerId(1);
		when(repository.findByCustomerId(1)).thenReturn(Optional.of(pref));

		Optional<CommunicationPreference> result = service.getPreference(1);
		assertTrue(result.isPresent());
	}

	@Test
	 void testDeletePreference() {
		service.deletePreference(1);
		verify(repository).deleteById(1); // ✅ match actual behavior
	}

	@Test
	 void testUpdatePreference() {
		CommunicationPreferenceRequest prefRequest = new CommunicationPreferenceRequest();
		prefRequest.setId(1);
		prefRequest.setCustomerId(1);
		prefRequest.setEmailOnServiceUpdates(true);

		CommunicationPreference existingPref = new CommunicationPreference();
		existingPref.setId(1);
		existingPref.setCustomerId(1);
		existingPref.setEmailOnServiceUpdates(false);

		when(repository.findByIdAndCustomerId(1, 1)).thenReturn(Optional.of(existingPref));
		when(repository.save(any(CommunicationPreference.class))).thenAnswer(invocation -> invocation.getArgument(0)); // ✅
																														// Match
																														// any
																														// and
																														// return
																														// what
																														// was
																														// passed

		CommunicationPreference updated = service.updatePreference(mapToEntity(prefRequest));

		assertTrue(updated.getEmailOnServiceUpdates());
	}

	@Test
	 void testGetPreferenceByIdAndCustomerId() {
		CommunicationPreference pref = new CommunicationPreference();
		pref.setId(1);
		pref.setCustomerId(1);
		when(repository.findByIdAndCustomerId(1, 1)).thenReturn(Optional.of(pref));

		Optional<CommunicationPreference> result = service.getPreferenceByIdAndCustomerId(1, 1);
		assertTrue(result.isPresent());
		assertEquals(1, result.get().getId());
		assertEquals(1, result.get().getCustomerId());
	}
}
