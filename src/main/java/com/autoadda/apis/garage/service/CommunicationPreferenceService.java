package com.autoadda.apis.garage.service;

import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.CommunicationPreference;
import com.autoadda.apis.garage.repository.CommunicationPreferenceRepository;
import com.autoadda.apis.garage.request.CommunicationPreferenceRequest;

@Service
public class CommunicationPreferenceService {

	@Autowired
	private CommunicationPreferenceRepository repository;

	CommunicationPreference mapToEntity(CommunicationPreferenceRequest preferenceRequest) {
		return CommunicationPreference.builder().customerId(preferenceRequest.getCustomerId())
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

	public CommunicationPreference saveOrUpdatePreference(CommunicationPreferenceRequest preference) {
		Optional<CommunicationPreference> existing = repository.findByCustomerId(preference.getCustomerId());
		if (existing.isPresent()) {
			CommunicationPreference old = existing.get();
			preference.setId(old.getId()); // Update
		}
		return repository.save(mapToEntity(preference));
	}

	public Optional<CommunicationPreference> getPreference(Integer customerId) {
		return repository.findByCustomerId(customerId);
	}

	public Optional<CommunicationPreference> getPreferenceByIdAndCustomerId(Integer id, Integer customerId) {
		return repository.findByIdAndCustomerId(id, customerId);
	}

	public Optional<CommunicationPreference> getPreferenceByCustomerIdAndStatus(Integer customerId, String status) {
		return repository.findByCustomerIdAndStatus(customerId, status);
	}

	public void deletePreference(Integer id) {
		repository.deleteById(id);
	}

	public CommunicationPreference updatePreference(CommunicationPreference preference) {
		Optional<CommunicationPreference> existing = repository.findByIdAndCustomerId(preference.getId(),
				preference.getCustomerId());
		if (existing.isPresent()) {
			return repository.save(preference);
		}
		return null; // or throw an exception
	}

	public boolean isWithinAllowedTime(CommunicationPreference pref, LocalTime now) {
		if (pref.getDoNotDisturbStart() == null || pref.getDoNotDisturbEnd() == null) {
			return true; // No restriction
		}

		// e.g., DND from 9 PM to 7 AM
		if (pref.getDoNotDisturbStart().isBefore(pref.getDoNotDisturbEnd())) {
			return !(now.isAfter(pref.getDoNotDisturbStart()) && now.isBefore(pref.getDoNotDisturbEnd()));
		} else {
			// Over midnight case
			return now.isBefore(pref.getDoNotDisturbEnd()) || now.isAfter(pref.getDoNotDisturbStart());
		}
	}

}
