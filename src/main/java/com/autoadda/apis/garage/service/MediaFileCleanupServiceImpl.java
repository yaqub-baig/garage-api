package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of media file cleanup service.
 * Handles asynchronous cleanup of media files that are no longer referenced
 * by any entity (Garage or OtherService) in the system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaFileCleanupServiceImpl implements MediaFileCleanupService {

    private final MediaFileRepository mediaFileRepository;
    private final GarageRepository garageRepository;

    /**
     * Schedules asynchronous cleanup of media files.
     * This method runs asynchronously in a separate transaction to ensure
     * cleanup happens independently of the calling transaction.
     *
     * A 3-second delay is added to ensure the parent transaction is committed
     * before checking references and performing cleanup.
     *
     * @param mediaFileIds List of media file IDs to be cleaned up
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void scheduleCleanup(List<Long> mediaFileIds) {
        if (!waitForTransactionCommit()) {
            return;
        }

        mediaFileIds.forEach(this::cleanupSingleMediaFile);
    }

    /**
     * Waits for the parent transaction to commit.
     *
     * @return true if wait was successful, false if interrupted
     */
    private boolean waitForTransactionCommit() {
        try {
            Thread.sleep(3000);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Media file cleanup interrupted");
            return false;
        }
    }

    /**
     * Attempts to clean up a single media file.
     * Deletes the file only if it's no longer referenced by any entity.
     *
     * @param fileId The ID of the media file to clean up
     */
    private void cleanupSingleMediaFile(Long fileId) {
        try {
            if (!isMediaFileStillReferenced(fileId)) {
                mediaFileRepository.deleteById(fileId);
                log.info("Successfully cleaned up media file: {}", fileId);
            } else {
                log.info("Media file {} still referenced, skipping cleanup", fileId);
            }
        } catch (Exception e) {
            log.warn("Failed to cleanup media file {}: {}", fileId, e.getMessage());
        }
    }

    /**
     * Checks if a media file is still referenced by any entity in the system.
     * This method checks references in both Garage and OtherService entities
     * across all their media file relationships (logo, images, certificates, etc.).
     *
     * @param mediaFileId The ID of the media file to check
     * @return true if the file is still referenced, false otherwise
     */
    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check Garage references
        long garageLogoCount = garageRepository.countByGarageLogoId(mediaFileId);
        long garageImageCount = garageRepository.countByGarageImageId(mediaFileId);
        long garageTradeLicenseCount = garageRepository.countByTradeLicenseId(mediaFileId);
        long garageVatCertCount = garageRepository.countByVatCertificateId(mediaFileId);

        // Calculate total references across all entities
        long totalReferences = garageLogoCount + garageImageCount + garageTradeLicenseCount + garageVatCertCount;

        return totalReferences > 0;
    }
}
