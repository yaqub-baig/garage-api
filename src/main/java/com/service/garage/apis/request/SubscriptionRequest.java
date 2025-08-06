package com.service.garage.apis.request;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Integer ownerId;
    private Integer planId;
}
