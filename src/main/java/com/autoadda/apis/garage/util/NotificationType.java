package com.autoadda.apis.garage.util;

/**
 * Enum representing different types of notifications that can be sent to customers.
 * Each type maps to specific preference fields in the CommunicationPreference entity.
 */
public enum NotificationType {
    /**
     * Service update notifications (job card status changes, work order updates, etc.)
     * Maps to: smsOnServiceUpdates, emailOnServiceUpdates, webpushOnServiceUpdates
     */
    SERVICE_UPDATES,

    /**
     * Appointment-related notifications (confirmations, reminders, etc.)
     * Maps to: smsOnAppointments, emailOnAppointments, webpushOnAppointments
     */
    APPOINTMENTS,

    /**
     * Reminder notifications (service due, appointment reminders, etc.)
     * Maps to: smsOnReminders, emailOnReminders, webpushOnReminders
     */
    REMINDERS,

    /**
     * Promotional offers and discounts
     * Maps to: smsOnOffers, emailOnOffers, webpushOnOffers
     */
    OFFERS,

    /**
     * Quotation and part request notifications
     * Maps to: smsOnQuotationRequests, emailOnQuotationRequests, webpushOnQuotationRequests
     */
    QUOTATION_REQUESTS,

    /**
     * Garage-Technician collaboration request notifications
     * Maps to: smsOnServiceUpdates, emailOnServiceUpdates, webpushOnServiceUpdates
     * (Uses SERVICE_UPDATES preferences for business-related communications)
     */
    COLLABORATION_REQUESTS
}
