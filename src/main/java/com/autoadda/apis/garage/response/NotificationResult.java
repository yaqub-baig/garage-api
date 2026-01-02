package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the aggregated result of sending a notification across multiple channels.
 */
@Data
@Slf4j
@AllArgsConstructor
public class NotificationResult {
    /**
     * Results for each channel that was attempted
     */
    private Map<Channel, ChannelResult> results;

    /**
     * Constructor with empty results map
     */

    public NotificationResult() {
        this.results = new EnumMap<>(Channel.class);
    }

    /**
     * Check if at least one channel succeeded
     *
     * @return true if any channel succeeded
     */
    public boolean hasAnySuccess() {
        return results.values().stream()
                .anyMatch(ChannelResult::isSuccess);
    }

    /**
     * Check if all attempted channels succeeded
     *
     * @return true if all channels succeeded
     */
    public boolean hasAllSuccess() {
        return !results.isEmpty() && results.values().stream()
                .allMatch(ChannelResult::isSuccess);
    }

    /**
     * Get list of channels that failed
     *
     * @return list of failed channels
     */
    public List<Channel> getFailedChannels() {
        return results.entrySet().stream()
                .filter(e -> !e.getValue().isSuccess())
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Get a summary string of the notification result
     *
     * @return summary in format "X/Y channels succeeded"
     */
    public String getSummary() {
        long successCount = results.values().stream()
                .filter(ChannelResult::isSuccess)
                .count();
        return String.format("%d/%d channels succeeded", successCount, results.size());
    }

    /**
     * Create a failed result with error message
     *
     * @param errorMessage the error message
     * @return NotificationResult with failure
     */
    public static NotificationResult failed(String errorMessage) {
        NotificationResult result = new NotificationResult();
        result.setResults(new EnumMap<>(Channel.class));
        log.info(errorMessage);
        return result;
    }

    /**
     * Add a channel result
     *
     * @param channel the channel
     * @param channelResult the result for that channel
     */
    public void addResult(Channel channel, ChannelResult channelResult) {
        this.results.put(channel, channelResult);
    }
}
