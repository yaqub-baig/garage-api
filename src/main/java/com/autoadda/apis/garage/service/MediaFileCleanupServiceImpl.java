package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.os.OtherServiceRepository;
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
    private final OtherServiceRepository otherServiceRepository;

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
        // Small delay to ensure transaction is committed
        try {
            Thread.sleep(3000);

            for (Long fileId : mediaFileIds) {
                try {
                    // Check if file is still referenced elsewhere before deleting
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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Media file cleanup interrupted");
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

        // Check OtherService references
        long otherServiceLogoCount = otherServiceRepository.countByCompanyLogoId(mediaFileId);
        long otherServiceVatCertCount = otherServiceRepository.countByVatCertificateId(mediaFileId);
        long otherServiceTradeLicenseCount = otherServiceRepository.countByTradeLicenseAttachmentId(mediaFileId);
        long otherServiceImagesCount = otherServiceRepository.countByCompanyImagesId(mediaFileId);

        // Calculate total references across all entities
        long totalReferences = garageLogoCount + garageImageCount + garageTradeLicenseCount + garageVatCertCount
                + otherServiceLogoCount + otherServiceVatCertCount + otherServiceTradeLicenseCount + otherServiceImagesCount;

        return totalReferences > 0;
    }
}
