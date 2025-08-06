package com.service.garage.apis.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.SubscriptionUsageLog;

@Repository
public interface SubscriptionUsageLogRepository extends JpaRepository<SubscriptionUsageLog, Integer> {
	// Additional query methods can be defined here if needed
	List<SubscriptionUsageLog> findBySubscriptionId(Integer subscriptionId);

	List<SubscriptionUsageLog> findByServiceName(String serviceName);

	List<SubscriptionUsageLog> findByTechnicianId(Integer technicianId);

	List<SubscriptionUsageLog> findByUsedOnBetween(LocalDate startDate, LocalDate endDate);

	List<SubscriptionUsageLog> findBySubscriptionIdAndUsedOnBetween(Integer subscriptionId, LocalDate startDate,
			LocalDate endDate);

	List<SubscriptionUsageLog> findBySubscriptionIdAndServiceName(Integer subscriptionId, String serviceName);

}
