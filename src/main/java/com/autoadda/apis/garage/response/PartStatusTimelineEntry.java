package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One step in a part request's timeline.
 * <p>
 * {@code level = REQUEST}: the whole request changed status ({@code event} is a {@code PartRequestStatus}).<br>
 * {@code level = ITEM}: something happened to one part ({@code event} is PART_PROCURED,
 * PART_RECEIVED_FROM_CUSTOMER or PART_ADDED_TO_INVOICE, with {@code partRequestItemId} / {@code partName}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartStatusTimelineEntry {
    private String level;
    private String event;
    private LocalDateTime timestamp;
    private String description;
    private Integer partRequestItemId;
    private String partName;
    /** Minutes since the previous entry (null for the first). */
    private Integer durationInMinutes;
}
