package com.autoadda.apis.garage.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Container for multi-channel notification content.
 * Holds message content for SMS, Email, and WebPush channels.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationContent {
    /**
     * SMS message content (plain text, concise, ~160 characters recommended)
     */
    private String smsMessage;

    /**
     * Email subject line
     */
    private String emailSubject;

    /**
     * Email body content
     */
    private String emailBody;

    /**
     * Flag indicating if email body is HTML formatted
     */
    private boolean emailIsHtml;

    /**
     * WebPush notification title
     */
    private String webpushTitle;

    /**
     * WebPush notification body
     */
    private String webpushBody;

    /**
     * WebPush notification deep link URL
     */
    private String webpushUrl;

    /**
     * WebPush notification icon URL (optional)
     */
    private String webpushIcon;

    /**
     * Optional metadata for template variables
     */
    private Map<String, String> metadata;
}
