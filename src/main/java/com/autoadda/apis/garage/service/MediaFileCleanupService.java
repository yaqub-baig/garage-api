package com.autoadda.apis.garage.service;

import java.util.List;

/**
 * Service interface for asynchronous media file cleanup operations.
 * This service handles cleanup of media files that are no longer referenced
 * by any entity in the system.
 */
public interface MediaFileCleanupService {

    /**
     * Schedules asynchronous cleanup of media files.
     * Files are only deleted if they are not referenced by any entity.
     * This method runs in a separate transaction to ensure cleanup
     * happens independently of the calling transaction.
     *
     * @param mediaFileIds List of media file IDs to be cleaned up
     */
    void scheduleCleanup(List<Long> mediaFileIds);
}
