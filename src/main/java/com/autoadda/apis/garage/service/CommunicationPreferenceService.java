package com.autoadda.apis.garage.service;

import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.CommunicationPreference;
import com.autoadda.apis.garage.repository.CommunicationPreferenceRepository;
import com.autoadda.apis.garage.request.CommunicationPreferenceRequest;

import jakarta.transaction.Transactional;

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

	/**
	 * Saves or updates the communication preference for a customer. If a preference
	 * already exists for the customer, it updates the existing one. Otherwise, it
	 * creates a new preference.
	 *
	 * @param preference The communication preference request containing the
	 *                   details.
	 * @return The saved or updated CommunicationPreference entity.
	 */
	@Transactional
	public CommunicationPreference saveOrUpdatePreference(CommunicationPreferenceRequest preference) {
		Optional<CommunicationPreference> existing = repository.findByCustomerId(preference.getCustomerId());
		if (existing.isPresent()) {
			CommunicationPreference old = existing.get();
			preference.setId(old.getId()); // Update
		}
		return repository.save(mapToEntity(preference));
	}

	/**
	 * Retrieves the communication preference for a specific customer.
	 *
	 * @param customerId The ID of the customer.
	 * @return An Optional containing the CommunicationPreference if found,
	 *         otherwise empty.
	 */
	@Transactional
	public Optional<CommunicationPreference> getPreference(Integer customerId) {
		return repository.findByCustomerId(customerId);
	}

	/**
	 * Retrieves the communication preference by its ID and customer ID.
	 *
	 * @param id         The ID of the communication preference.
	 * @param customerId The ID of the customer.
	 * @return An Optional containing the CommunicationPreference if found,
	 *         otherwise empty.
	 */
	@Transactional
	public Optional<CommunicationPreference> getPreferenceByIdAndCustomerId(Integer id, Integer customerId) {
		return repository.findByIdAndCustomerId(id, customerId);
	}

	/**
	 * Retrieves the communication preference by customer ID and status.
	 *
	 * @param customerId The ID of the customer.
	 * @param status     The status of the communication preference.
	 * @return An Optional containing the CommunicationPreference if found,
	 *         otherwise empty.
	 */
	@Transactional
	public Optional<CommunicationPreference> getPreferenceByCustomerIdAndStatus(Integer customerId, String status) {
		return repository.findByCustomerIdAndStatus(customerId, status);
	}

	/**
	 * Deletes the communication preference by its ID.
	 *
	 * @param id The ID of the communication preference to delete.
	 */
	@Transactional
	public void deletePreference(Integer id) {
		repository.deleteById(id);
	}

	/**
	 * Updates the communication preference for a customer.
	 *
	 * @param preference The CommunicationPreference entity to update.
	 * @return The updated CommunicationPreference entity if it exists, otherwise
	 *         null.
	 */
	@Transactional
	public CommunicationPreference updatePreference(CommunicationPreference preference) {
		Optional<CommunicationPreference> existing = repository.findByIdAndCustomerId(preference.getId(),
				preference.getCustomerId());
		if (existing.isPresent()) {
			return repository.save(preference);
		}
		return null; // or throw an exception
	}

	/**
	 * Checks if the current time is within the allowed communication time based on
	 * the customer's do-not-disturb preferences.
	 *
	 * @param pref The CommunicationPreference entity containing DND times.
	 * @param now  The current LocalTime to check against.
	 * @return true if communication is allowed, false if within DND period.
	 */
	public boolean isWithinAllowedTime(CommunicationPreference pref, LocalTime now) {
		if (pref.getDoNotDisturbStart() == null || pref.getDoNotDisturbEnd() == null) {
			return true; // No DND restriction
		}

		LocalTime start = pref.getDoNotDisturbStart();
		LocalTime end = pref.getDoNotDisturbEnd();

		if (start.equals(end)) {
			return false; // Full-day DND
		}

		if (start.isBefore(end)) {
			// Same-day DND window
			return now.isBefore(start) || now.isAfter(end);
		} else {
			// Cross-midnight DND window (e.g., 9PM–7AM)
			return !(now.isAfter(start) || now.isBefore(end));
		}
	}

}
