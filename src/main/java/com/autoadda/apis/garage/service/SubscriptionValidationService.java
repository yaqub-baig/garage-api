package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.GarageSubscription;
import com.autoadda.apis.garage.entity.VehicleOwnerSubscription;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.VehicleOwnerSubscriptionRepository;
import com.autoadda.apis.garage.util.SubscriptionStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionValidationService {

    private final VehicleOwnerSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Validates that a subscription can be used for booking.
     * Checks:
     * 1. Subscription exists and belongs to customer
     * 2. Subscription is ACTIVE
     * 3. Subscription has not expired
     * 4. Subscription is for the correct garage
     * 5. All selected services are included in subscription
     * 6. Service quantity limits are not exceeded
     *
     * @param subscriptionId The subscription ID to validate
     * @param customerId The customer making the booking
     * @param garageId The garage being booked
     * @param selectedServiceIds Comma-separated service IDs
     * @return The validated subscription
     * @throws ResourceNotFoundException if subscription not found
     * @throws ConflictException if subscription is invalid/expired
     */
    public VehicleOwnerSubscription validateSubscriptionForBooking(
            Integer subscriptionId,
            Integer customerId,
            Integer garageId,
            String selectedServiceIds) {

        // 1. Fetch subscription
        VehicleOwnerSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found with ID: " + subscriptionId));

        // 2. Verify ownership
        if (!subscription.getCustomerId().equals(customerId)) {
            log.warn("Customer {} attempted to use subscription {} owned by customer {}",
                    customerId, subscriptionId, subscription.getCustomerId());
            throw new ConflictException("This subscription does not belong to you.");
        }

        // 3. Check status is ACTIVE
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ConflictException(
                    String.format("Subscription is %s. Only ACTIVE subscriptions can be used.",
                            subscription.getStatus()));
        }

        // 4. Check not expired
        if (subscription.getEndDate().isBefore(LocalDate.now())) {
            throw new ConflictException(
                    String.format("Subscription expired on %s. Please renew to continue using benefits.",
                            subscription.getEndDate()));
        }

        // 5. Verify garage matches
        GarageSubscription plan = subscription.getGarageSubscription();
        if (!plan.getGarage().getId().equals(garageId)) {
            throw new ConflictException(
                    String.format("This subscription is for %s, but you're booking at a different garage.",
                            plan.getGarage().getName()));
        }

        // 6. Parse selected service IDs
        List<Integer> serviceIds = Arrays.stream(selectedServiceIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();

        // 7. Get subscription's included services
        List<Integer> includedServiceIds = plan.getServicesIncluded().stream()
                .map(AvailableService::getId)
                .toList();

        // 8. Check all services are included
        for (Integer serviceId : serviceIds) {
            if (!includedServiceIds.contains(serviceId)) {
                throw new ConflictException(
                        "Service ID " + serviceId + " is not included in your subscription plan.");
            }
        }

        // 9. Check quantity limits (from serviceQuantity map)
        Map<Integer, Integer> serviceQuantityLimits = plan.getServiceQuantity();
        Map<String, Integer> currentUsage = parseUsageCounts(subscription.getUsageCounts());

        for (Integer serviceId : serviceIds) {
            Integer limit = serviceQuantityLimits != null ? serviceQuantityLimits.get(serviceId) : null;
            if (limit != null) {
                Integer used = currentUsage.getOrDefault(serviceId.toString(), 0);
                if (used >= limit) {
                    throw new ConflictException(
                            String.format("Service usage limit reached. You've used %d of %d allowed services.",
                                    used, limit));
                }
            }
        }

        log.info("Subscription {} validated successfully for customer {} at garage {}",
                subscriptionId, customerId, garageId);

        return subscription;
    }

    /**
     * Checks if customer has any active subscriptions for a garage.
     *
     * @param customerId Customer ID
     * @param garageId Garage ID
     * @return List of active subscriptions
     */
    public List<VehicleOwnerSubscription> getActiveSubscriptionsForGarage(
            Integer customerId, Integer garageId) {

        return subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE)
                .stream()
                .filter(sub -> sub.getCustomerId().equals(customerId))
                .filter(sub -> sub.getGarageSubscription().getGarage().getId().equals(garageId))
                .filter(sub -> sub.getEndDate().isAfter(LocalDate.now()))
                .toList();
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
}
