package com.autoadda.apis.garage.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class MonthlyAvailabilityResponse {
    private Integer garageId;
    private Integer year;
    private Integer month;
    private Map<LocalDate, Boolean> dailyAvailability;

    // Helper method to get availability for a specific day
    public Boolean isAvailableOnDate(LocalDate date) {
        return dailyAvailability.getOrDefault(date, false);
    }

    // Helper method to get count of available days
    public Long getAvailableDaysCount() {
        return dailyAvailability.values().stream()
                .mapToLong(available -> available ? 1 : 0)
                .sum();
    }
}
