package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.repository.AppointmentServiceRepository;
import com.autoadda.apis.garage.repository.SubscriptionUsageLogRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerSubscriptionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionUsageTrackingService {

    private final VehicleOwnerSubscriptionRepository subscriptionRepository;
    private final SubscriptionUsageLogRepository usageLogRepository;
    private final AppointmentServiceRepository appointmentServiceRepository;
    private final ObjectMapper objectMapper;

    /**
     * Track subscription usage when appointment is completed.
     * Updates usage counts and creates usage log entries.
     *
     * @param appointment The completed appointment
     * @param jobCard The job card for tracking (optional)
     */
    @Transactional
    public void trackSubscriptionUsage(AppointmentDetails appointment, JobCard jobCard) {
        if (appointment.getGarageSubscriptionPlanId() == null) {
            log.debug("No subscription used for appointment {}", appointment.getId());
            return;
        }

        // Parse subscription plan ID
        Integer subscriptionPlanId;
        try {
            subscriptionPlanId = Integer.parseInt(appointment.getGarageSubscriptionPlanId());
        } catch (NumberFormatException e) {
            log.warn("Invalid subscription plan ID: {}", appointment.getGarageSubscriptionPlanId());
            return;
        }

        // Find the customer's subscription
        List<VehicleOwnerSubscription> subscriptions = subscriptionRepository
                .findByCustomerIdAndGarageSubscriptionId(
                        appointment.getCustomerId(),
                        subscriptionPlanId
                );

        if (subscriptions.isEmpty()) {
            log.warn("Subscription plan {} not found for customer {}",
                    subscriptionPlanId, appointment.getCustomerId());
            return;
        }

        VehicleOwnerSubscription subscription = subscriptions.get(0);

        // Get appointment services
        List<AppointmentServices> services = appointmentServiceRepository
                .findByAppointmentId(appointment.getId());

        if (services.isEmpty()) {
            log.debug("No services found for appointment {}", appointment.getId());
            return;
        }

        // Update usage counts
        Map<String, Integer> usageCounts = parseUsageCounts(subscription.getUsageCounts());

        for (AppointmentServices service : services) {
            if (service.getService() == null || service.getService().getAvailableService() == null) {
                continue;
            }

            Integer serviceId = service.getService().getAvailableService().getId();
            String serviceName = service.getService().getAvailableService().getName();

            // Check if service is included in subscription
            boolean isIncluded = subscription.getGarageSubscription().getServicesIncluded().stream()
                    .anyMatch(s -> s.getId().equals(serviceId));

            if (isIncluded) {
                // Increment usage count
                usageCounts.merge(serviceId.toString(), 1, Integer::sum);

                // Create usage log entry
                SubscriptionUsageLog usageLog = new SubscriptionUsageLog();
                usageLog.setSubscription(subscription);
                usageLog.setServiceName(serviceName);
                usageLog.setUsedOn(LocalDate.now());
                usageLog.setJobCard(jobCard);
                if (service.getTechnician() != null) {
                    usageLog.setTechnician(service.getTechnician());
                }
                usageLogRepository.save(usageLog);

                log.info("Tracked usage for service '{}' (ID: {}) under subscription {}",
                        serviceName, serviceId, subscription.getId());
            }
        }

        // Save updated usage counts
        subscription.setUsageCounts(serializeUsageCounts(usageCounts));
        subscriptionRepository.save(subscription);

        log.info("Successfully tracked usage for appointment {} under subscription {}",
                appointment.getId(), subscription.getId());
    }

    /**
     * Parse usage counts from JSON string.
     *
     * @param usageCountsJson JSON string representing usage counts
     * @return Map of service ID to usage count
     */
    private Map<String, Integer> parseUsageCounts(String usageCountsJson) {
        if (usageCountsJson == null || usageCountsJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(usageCountsJson, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse usage counts JSON: {}", usageCountsJson, e);
            return new HashMap<>();
        }
    }

    /**
     * Serialize usage counts to JSON string.
     *
     * @param usageCounts Map of service ID to usage count
     * @return JSON string
     */
    private String serializeUsageCounts(Map<String, Integer> usageCounts) {
        try {
            return objectMapper.writeValueAsString(usageCounts);
        } catch (Exception e) {
            log.error("Failed to serialize usage counts", e);
            return "{}";
        }
    }

    /**
     * Get current usage count for a specific service in a subscription.
     *
     * @param subscriptionId Subscription ID
     * @param serviceId Service ID
     * @return Current usage count
     */
    public int getServiceUsageCount(Integer subscriptionId, Integer serviceId) {
        VehicleOwnerSubscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            return 0;
        }

        Map<String, Integer> usageCounts = parseUsageCounts(subscription.getUsageCounts());
        return usageCounts.getOrDefault(serviceId.toString(), 0);
    }

    /**
     * Check if a service usage is within the subscription limit.
     *
     * @param subscriptionId Subscription ID
     * @param serviceId Service ID
     * @return true if usage is within limit, false otherwise
     */
    public boolean isWithinUsageLimit(Integer subscriptionId, Integer serviceId) {
        VehicleOwnerSubscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            return false;
        }

        Map<Integer, Integer> serviceQuantityLimits = subscription.getGarageSubscription().getServiceQuantity();
        Integer limit = serviceQuantityLimits != null ? serviceQuantityLimits.get(serviceId) : null;

        // If no limit defined, assume unlimited
        if (limit == null) {
            return true;
        }

        int currentUsage = getServiceUsageCount(subscriptionId, serviceId);
        return currentUsage < limit;
    }
}
