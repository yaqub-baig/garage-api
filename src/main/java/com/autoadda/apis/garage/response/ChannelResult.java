package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the result of sending a notification through a specific channel.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChannelResult {
    /**
     * Whether the notification was successfully sent through this channel
     */
    private boolean success;

    /**
     * Status message or error details
     */
    private String message;
}
