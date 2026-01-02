package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.CommunicationPreference;
import com.autoadda.apis.garage.request.EmailRequest;
import com.autoadda.apis.garage.request.NotificationContent;
import com.autoadda.apis.garage.request.SendNotificationRequest;
import com.autoadda.apis.garage.request.SmsRequest;
import com.autoadda.apis.garage.response.ChannelResult;
import com.autoadda.apis.garage.response.CustomerContactInfo;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.NotificationResult;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import com.autoadda.apis.garage.util.Channel;
import com.autoadda.apis.garage.util.NotificationType;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Centralized service for sending notifications with communication preference checking.
 * Respects user preferences for SMS, Email, and WebPush channels.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private final AuthClient authClient;
    private final VehicleClient vehicleClient;

    /**
     * Send notification with preference checking
     *
     * @param customerId       Customer ID to fetch preferences
     * @param notificationType Type of notification
     * @param content          Multi-channel message content
     * @param isCritical       If true, SMS sent even if all channels disabled
     * @param targetRole       Role to send WebPush notification to (e.g., "CUSTOMER", "GARAGE_TECH", "GARAGE_OWNER")
     * @return Result of notification dispatch
     */
    public NotificationResult sendNotification(
            Integer customerId,
            NotificationType notificationType,
            NotificationContent content,
            boolean isCritical,
            String targetRole) {

        log.info("Sending {} notification to customer {}, isCritical={}",
                 notificationType, customerId, isCritical);

        try {
            // Fetch communication preferences
            CommunicationPreference preferences = fetchPreferencesWithFallback(customerId);

            // Determine enabled channels based on preferences
            Set<Channel> enabledChannels = determineEnabledChannels(preferences, notificationType);

            // Apply critical override if needed
            enabledChannels = applyCriticalOverride(enabledChannels, isCritical, notificationType);

            if (enabledChannels.isEmpty()) {
                log.warn("No channels enabled for customer {} notification type {} (non-critical)",
                         customerId, notificationType);
                return NotificationResult.failed("No channels enabled for this notification type");
            }

            // Get customer contact info
            CustomerContactInfo contactInfo = getCustomerContactInfoById(customerId);

            // Send to enabled channels
            NotificationResult result = sendToChannels(enabledChannels, content, contactInfo, targetRole);

            log.info("Notification dispatch completed for customer {}: {}",
                     customerId, result.getSummary());
            return result;

        } catch (Exception e) {
            log.error("Failed to send notification to customer {}: {}",
                      customerId, e.getMessage(), e);
            return handleNotificationFailure(customerId, content, isCritical, targetRole, e);
        }
    }

    /**
     * Send notification by vehicle ID (fetches customer from vehicle owner)
     *
     * @param vehicleId        Vehicle ID
     * @param notificationType Type of notification
     * @param content          Multi-channel message content
     * @param isCritical       If true, SMS sent even if all channels disabled
     * @param targetRole       Role to send WebPush notification to (e.g., "CUSTOMER", "GARAGE_TECH", "GARAGE_OWNER")
     * @return Result of notification dispatch
     */
    public NotificationResult sendNotificationByVehicleId(
            Integer vehicleId,
            NotificationType notificationType,
            NotificationContent content,
            boolean isCritical,
            String targetRole) {

        try {
            VehicleDetailResponse vehicle = vehicleClient.getVehicleDetailsByIdResponse(vehicleId);
            if (vehicle == null || vehicle.getOwner() == null) {
                log.error("Cannot send notification: vehicle {} or owner not found", vehicleId);
                return NotificationResult.failed("Vehicle or owner not found");
            }

            Integer customerId = vehicle.getOwner().getId();
            return sendNotification(customerId, notificationType, content, isCritical, targetRole);

        } catch (Exception e) {
            log.error("Failed to send notification for vehicle {}: {}",
                      vehicleId, e.getMessage(), e);
            return NotificationResult.failed("Failed to get vehicle details: " + e.getMessage());
        }
    }

    /**
     * Fetch communication preferences with fallback to defaults
     */
    private CommunicationPreference fetchPreferencesWithFallback(Integer customerId) {
        try {
            ResponseEntity<CommunicationPreference> response =
                    authClient.getPreferencesByCustomerId1(customerId);

            if (response.getStatusCode().is2xxSuccessful()) {
                CommunicationPreference prefs = response.getBody();
                if (prefs != null) {
                    log.debug("Fetched preferences for customer {}", customerId);
                    return prefs;
                }
            }

            log.warn("Preferences API returned non-success or null for customer: {}", customerId);
            return createDefaultPreferences(customerId);

        } catch (FeignException e) {
            log.error("Failed to fetch preferences for customer: {}. Error: {}",
                      customerId, e.getMessage());
            return createDefaultPreferences(customerId);
        }
    }

    /**
     * Create default preferences (all channels enabled - opt-out model)
     */
    private CommunicationPreference createDefaultPreferences(Integer customerId) {
        log.info("Using default preferences (all enabled) for customer {}", customerId);
        return CommunicationPreference.builder()
                .customerId(customerId)
                .smsOnServiceUpdates(true)
                .emailOnServiceUpdates(true)
                .webpushOnServiceUpdates(true)
                .smsOnAppointments(true)
                .emailOnAppointments(true)
                .webpushOnAppointments(true)
                .smsOnReminders(true)
                .emailOnReminders(true)
                .webpushOnReminders(true)
                .smsOnOffers(true)
                .emailOnOffers(true)
                .webpushOnOffers(true)
                .smsOnQuotationRequests(true)
                .emailOnQuotationRequests(true)
                .webpushOnQuotationRequests(true)
                .build();
    }

    /**
     * Determine which channels are enabled based on preference and notification type
     */
    private Set<Channel> determineEnabledChannels(
            CommunicationPreference prefs,
            NotificationType type) {

        Set<Channel> enabledChannels = new HashSet<>();

        switch (type) {
            case SERVICE_UPDATES, COLLABORATION_REQUESTS:
                // COLLABORATION_REQUESTS uses SERVICE_UPDATES preferences
                addChannelsIfEnabled(enabledChannels,
                        prefs.getSmsOnServiceUpdates(),
                        prefs.getEmailOnServiceUpdates(),
                        prefs.getWebpushOnServiceUpdates());
                break;

            case APPOINTMENTS:
                addChannelsIfEnabled(enabledChannels,
                        prefs.getSmsOnAppointments(),
                        prefs.getEmailOnAppointments(),
                        prefs.getWebpushOnAppointments());
                break;

            case REMINDERS:
                addChannelsIfEnabled(enabledChannels,
                        prefs.getSmsOnReminders(),
                        prefs.getEmailOnReminders(),
                        prefs.getWebpushOnReminders());
                break;

            case OFFERS:
                addChannelsIfEnabled(enabledChannels,
                        prefs.getSmsOnOffers(),
                        prefs.getEmailOnOffers(),
                        prefs.getWebpushOnOffers());
                break;

            case QUOTATION_REQUESTS:
                addChannelsIfEnabled(enabledChannels,
                        prefs.getSmsOnQuotationRequests(),
                        prefs.getEmailOnQuotationRequests(),
                        prefs.getWebpushOnQuotationRequests());
                break;
        }

        log.debug("Enabled channels for {}: {}", type, enabledChannels);
        return enabledChannels;
    }

    /**
     * Helper method to add channels to the set if their preferences are enabled
     */
    private void addChannelsIfEnabled(Set<Channel> channels, Boolean smsEnabled, Boolean emailEnabled, Boolean webpushEnabled) {
        if (Boolean.TRUE.equals(smsEnabled)) {
            channels.add(Channel.SMS);
        }
        if (Boolean.TRUE.equals(emailEnabled)) {
            channels.add(Channel.EMAIL);
        }
        if (Boolean.TRUE.equals(webpushEnabled)) {
            channels.add(Channel.WEBPUSH);
        }
    }

    /**
     * Apply critical notification override (send SMS if all channels disabled)
     */
    private Set<Channel> applyCriticalOverride(
            Set<Channel> enabledChannels,
            boolean isCritical,
            NotificationType type) {

        if (isCritical && enabledChannels.isEmpty()) {
            log.warn("Critical notification type {} has all channels disabled. " +
                     "Sending SMS override.", type);
            enabledChannels.add(Channel.SMS);
        }
        return enabledChannels;
    }

    /**
     * Get customer contact information by customer ID
     */
    private CustomerContactInfo getCustomerContactInfoById(Integer customerId) {
        try {
            UserResponse user = authClient.getUser(customerId);
            if (user == null) {
                throw new IllegalStateException("User not found for customer ID: " + customerId);
            }

            return CustomerContactInfo.builder()
                    .userId(customerId)
                    .phoneNumber(user.getMobileNumber())
                    .email(user.getEmail())
                    .build();

        } catch (Exception e) {
            log.error("Failed to fetch customer contact info for customer {}: {}",
                      customerId, e.getMessage());
            throw new IllegalStateException("Cannot fetch customer contact info", e);
        }
    }

    /**
     * Send notifications to enabled channels
     */
    private NotificationResult sendToChannels(
            Set<Channel> channels,
            NotificationContent content,
            CustomerContactInfo contactInfo,
            String targetRole) {

        NotificationResult result = new NotificationResult();

        for (Channel channel : channels) {
            ChannelResult channelResult = sendToChannel(channel, content, contactInfo, targetRole);
            result.addResult(channel, channelResult);
        }

        return result;
    }

    /**
     * Send notification to a specific channel
     */
    private ChannelResult sendToChannel(Channel channel, NotificationContent content,
                                       CustomerContactInfo contactInfo, String targetRole) {
        try {
            switch (channel) {
                case SMS:
                    return sendSms(content, contactInfo);
                case EMAIL:
                    return sendEmail(content, contactInfo);
                case WEBPUSH:
                    return sendWebPush(content, contactInfo, targetRole);
                default:
                    return new ChannelResult(false, "Unknown channel type");
            }
        } catch (Exception e) {
            log.error("Failed to send {} notification: {}", channel, e.getMessage());
            return new ChannelResult(false, e.getMessage());
        }
    }

    /**
     * Send SMS notification
     */
    private ChannelResult sendSms(NotificationContent content, CustomerContactInfo contactInfo) {
        if (contactInfo.getPhoneNumber() == null || contactInfo.getPhoneNumber().isBlank()) {
            log.warn("Skipping SMS channel: no phone number for customer {}", contactInfo.getUserId());
            return new ChannelResult(false, "NO_PHONE_NUMBER");
        }

        GenericResponse smsResult = authClient.sendSms(
                new SmsRequest(contactInfo.getPhoneNumber(), content.getSmsMessage())
        );
        return new ChannelResult("SUCCESS".equalsIgnoreCase(smsResult.getStatus()),
                smsResult.getMessage());
    }

    /**
     * Send Email notification
     */
    private ChannelResult sendEmail(NotificationContent content, CustomerContactInfo contactInfo) {
        if (contactInfo.getEmail() == null || contactInfo.getEmail().isBlank()) {
            log.warn("Skipping EMAIL channel: no email address for customer {}", contactInfo.getUserId());
            return new ChannelResult(false, "NO_EMAIL_ADDRESS");
        }

        GenericResponse emailResult = authClient.sendEmail(
                contactInfo.getUserId(),
                new EmailRequest(
                        contactInfo.getEmail(),
                        content.getEmailSubject(),
                        content.getEmailBody()
                )
        );
        return new ChannelResult("SUCCESS".equalsIgnoreCase(emailResult.getStatus()),
                emailResult.getMessage());
    }

    /**
     * Send WebPush notification
     */
    private ChannelResult sendWebPush(NotificationContent content, CustomerContactInfo contactInfo,
                                     String targetRole) {
        if (targetRole == null || targetRole.isEmpty()) {
            log.warn("Skipping WebPush for customer {}: No target role specified",
                    contactInfo.getUserId());
            return new ChannelResult(false, "NO_TARGET_ROLE_SPECIFIED");
        }

        ResponseEntity<Map<String, Object>> pushResult =
                authClient.sendNotification(
                        new SendNotificationRequest(
                                contactInfo.getUserId().toString(),
                                targetRole,
                                content.getWebpushTitle(),
                                content.getWebpushBody(),
                                content.getWebpushUrl()
                        )
                );

        log.info("Sent WebPush to user {} with role {}", contactInfo.getUserId(), targetRole);

        return new ChannelResult(pushResult.getStatusCode().is2xxSuccessful(),
                pushResult.getStatusCode().toString());
    }

    /**
     * Handle notification failure for critical notifications
     */
    private NotificationResult handleNotificationFailure(
            Integer customerId,
            NotificationContent content,
            boolean isCritical,
            String targetRole,
            Exception error) {

        log.error("Preference API failure for customer {}: {}", customerId, error.getMessage());

        if (isCritical) {
            log.warn("Sending critical notification anyway via SMS as emergency fallback");
            try {
                CustomerContactInfo contactInfo = getCustomerContactInfoById(customerId);
                Set<Channel> smsOnly = Set.of(Channel.SMS);
                return sendToChannels(smsOnly, content, contactInfo, targetRole);
            } catch (Exception e) {
                log.error("Emergency SMS fallback also failed: {}", e.getMessage());
            }
        }
        return NotificationResult.failed("Notification failed: " + error.getMessage());
    }
}
