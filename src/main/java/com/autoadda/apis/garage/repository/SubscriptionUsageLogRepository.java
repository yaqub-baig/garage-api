package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.SubscriptionUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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
