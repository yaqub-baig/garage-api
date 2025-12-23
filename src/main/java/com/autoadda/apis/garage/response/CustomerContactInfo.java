package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains customer contact information for sending notifications.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerContactInfo {
    /**
     * Customer/User ID
     */
    private Integer userId;

    /**
     * Customer phone number for SMS
     */
    private String phoneNumber;

    /**
     * Customer email address
     */
    private String email;
}
