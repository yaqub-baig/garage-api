package com.service.garage.apis.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.SubscriptionUsageLog;
import com.service.garage.apis.repository.SubscriptionUsageLogRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionUsageLogService {

	@Autowired
	private final SubscriptionUsageLogRepository usageLogRepository;

	/**
	 * Saves a subscription usage log.
	 *
	 * @param log the subscription usage log to save
	 * @return the saved subscription usage log
	 */
	@Transactional
	public SubscriptionUsageLog saveUsageLog(SubscriptionUsageLog log) {
		return usageLogRepository.save(log);
	}

	/**
	 * Retrieves all subscription usage logs.
	 *
	 * @return a list of all subscription usage logs
	 */
	@Transactional
	public List<SubscriptionUsageLog> getAllLogs() {
		return usageLogRepository.findAll();
	}

	/**
	 * Retrieves a subscription usage log by its ID.
	 *
	 * @param id the ID of the subscription usage log
	 * @return the subscription usage log with the specified ID
	 */
	@Transactional
	public SubscriptionUsageLog getLogById(Integer id) {
		return usageLogRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usage log not found for id: " + id));
	}

	/**
	 * Retrieves subscription usage logs by subscription ID.
	 *
	 * @param subscriptionId the ID of the subscription
	 * @return a list of subscription usage logs for the specified subscription
	 */
	@Transactional
	public List<SubscriptionUsageLog> getLogsBySubscriptionId(Integer subscriptionId) {
		return usageLogRepository.findBySubscriptionId(subscriptionId);
	}

	/**
	 * Retrieves subscription usage logs by service name.
	 *
	 * @param serviceName the name of the service
	 * @return a list of subscription usage logs for the specified service
	 */
	@Transactional
	public List<SubscriptionUsageLog> getLogsByServiceName(String serviceName) {
		return usageLogRepository.findByServiceName(serviceName);
	}

	/**
	 * Retrieves subscription usage logs by technician ID.
	 *
	 * @param technicianId the ID of the technician
	 * @return a list of subscription usage logs for the specified technician
	 */
	@Transactional
	public List<SubscriptionUsageLog> getLogsByTechnicianId(Integer technicianId) {
		return usageLogRepository.findByTechnicianId(technicianId);
	}

	/**
	 * Retrieves subscription usage logs by date range.
	 *
	 * @param startDate the start date of the range
	 * @param endDate   the end date of the range
	 * @return a list of subscription usage logs within the specified date range
	 */
	@Transactional
	public List<SubscriptionUsageLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
		return usageLogRepository.findByUsedOnBetween(startDate, endDate);
	}

	/**
	 * Retrieves subscription usage logs by subscription ID and date range.
	 *
	 * @param subscriptionId the ID of the subscription
	 * @param startDate      the start date of the range
	 * @param endDate        the end date of the range
	 * @return a list of subscription usage logs for the specified subscription
	 *         within the date range
	 */
	@Transactional
	public List<SubscriptionUsageLog> getLogsBySubscriptionIdAndDateRange(Integer subscriptionId, LocalDate startDate,
			LocalDate endDate) {
		return usageLogRepository.findBySubscriptionIdAndUsedOnBetween(subscriptionId, startDate, endDate);
	}

	/**
	 * Retrieves subscription usage logs by subscription ID and service name.
	 *
	 * @param subscriptionId the ID of the subscription
	 * @param serviceName    the name of the service
	 * @return a list of subscription usage logs for the specified subscription and
	 *         service
	 */
	@Transactional
	public List<SubscriptionUsageLog> getLogsBySubscriptionIdAndServiceName(Integer subscriptionId,
			String serviceName) {
		return usageLogRepository.findBySubscriptionIdAndServiceName(subscriptionId, serviceName);
	}

	/**
	 * Deletes a subscription usage log by its ID.
	 *
	 * @param id the ID of the subscription usage log to delete
	 */
	@Transactional
	public void deleteLog(Integer id) {
		if (!usageLogRepository.existsById(id)) {
			throw new RuntimeException("Cannot delete. Log not found for id: " + id);
		}
		usageLogRepository.deleteById(id);
	}
}
