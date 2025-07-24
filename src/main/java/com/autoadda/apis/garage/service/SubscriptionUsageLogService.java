package com.autoadda.apis.garage.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.SubscriptionUsageLog;
import com.autoadda.apis.garage.repository.SubscriptionUsageLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionUsageLogService {

	private final SubscriptionUsageLogRepository usageLogRepository;

	public SubscriptionUsageLog saveUsageLog(SubscriptionUsageLog log) {
		return usageLogRepository.save(log);
	}

	public List<SubscriptionUsageLog> getAllLogs() {
		return usageLogRepository.findAll();
	}

	public SubscriptionUsageLog getLogById(Integer id) {
		return usageLogRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Usage log not found for id: " + id));
	}

	public List<SubscriptionUsageLog> getLogsBySubscriptionId(Integer subscriptionId) {
		return usageLogRepository.findBySubscriptionId(subscriptionId);
	}

	public List<SubscriptionUsageLog> getLogsByServiceName(String serviceName) {
		return usageLogRepository.findByServiceName(serviceName);
	}

	public List<SubscriptionUsageLog> getLogsByTechnicianId(Integer technicianId) {
		return usageLogRepository.findByTechnicianId(technicianId);
	}

	public List<SubscriptionUsageLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
		return usageLogRepository.findByUsedOnBetween(startDate, endDate);
	}

	public List<SubscriptionUsageLog> getLogsBySubscriptionIdAndDateRange(Integer subscriptionId, LocalDate startDate,
			LocalDate endDate) {
		return usageLogRepository.findBySubscriptionIdAndUsedOnBetween(subscriptionId, startDate, endDate);
	}

	public List<SubscriptionUsageLog> getLogsBySubscriptionIdAndServiceName(Integer subscriptionId,
			String serviceName) {
		return usageLogRepository.findBySubscriptionIdAndServiceName(subscriptionId, serviceName);
	}

	public void deleteLog(Integer id) {
		if (!usageLogRepository.existsById(id)) {
			throw new RuntimeException("Cannot delete. Log not found for id: " + id);
		}
		usageLogRepository.deleteById(id);
	}
}
