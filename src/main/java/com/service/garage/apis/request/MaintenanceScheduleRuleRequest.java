package com.service.garage.apis.request;

import com.service.garage.apis.util.IntervalType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceScheduleRuleRequest {
    @JsonProperty("make")
    private String make;

    @JsonProperty("model")
    private String model;

    @JsonProperty("serviceType")
    private String serviceType;

    @JsonProperty("intervalType")
    private IntervalType intervalType;

    @JsonProperty("intervalValue")
    private Integer intervalValue;

    @JsonProperty("isActive")
    private boolean isActive;
    
    @Override
    public String toString() {
        return "MaintenanceScheduleRuleRequest{" +
                "make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", intervalType=" + intervalType +
                ", intervalValue=" + intervalValue +
                ", isActive=" + isActive +
                '}';
    }
}
