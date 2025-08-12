package com.autoadda.apis.garage.request;

import lombok.Data;

@Data
public class SubscriptionRequest {
    private Integer ownerId;
    private Integer planId;
}
