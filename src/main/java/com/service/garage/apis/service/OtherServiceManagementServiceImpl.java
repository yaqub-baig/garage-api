package com.service.garage.apis.service;

import com.service.garage.apis.entity.Location;
import com.service.garage.apis.entity.MediaFile;
import com.service.garage.apis.entity.OtherService;
import com.service.garage.apis.exception.AlreadyExistsException;
import com.service.garage.apis.exception.ResourceNotFoundException;
import com.service.garage.apis.repository.MediaFileRepository;
import com.service.garage.apis.repository.OtherServiceRepository;
import com.service.garage.apis.request.OtherServiceRequest;
import com.service.garage.apis.util.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtherServiceManagementServiceImpl implements OtherServiceManagementService {

    private final OtherServiceRepository otherServiceRepository;
    private final LocationService locationService;
    private final MediaFileService mediaFileService;
    private final MediaFileRepository mediaFileRepository;

    private OtherService mapToEntity(OtherServiceRequest request) {
        OtherService otherService = new OtherService ( );
        otherService.setCompanyBrandName ( request.getCompanyBrandName ( ) );
        otherService.setName ( request.getName ( ) );
        otherService.setEmail ( request.getEmail ( ) );
        otherService.setPhoneNumber ( request.getPhoneNumber ( ) );
        otherService.setMobileNumber ( request.getMobileNumber ( ) );
        otherService.setTrnNumber ( request.getTrnNumber ( ) );
        otherService.setTradeLicenceNumber ( request.getTradeLicenceNumber ( ) );
        otherService.setOpeningTime ( request.getOpeningTime ( ) );
        otherService.setClosingTime ( request.getClosingTime ( ) );
        otherService.setWorkingDays ( request.getWorkingDays ( ) );
        otherService.setWebSite ( request.getWebSite ( ) );
        otherService.setAccountStatus ( Status.PENDING ); // Default status
        otherService.setServiceType ( request.getServiceType ( ) );
        otherService.setCompanyArabicName ( request.getCompanyArabicName ( ) );
        otherService.setCreatedAt ( LocalDateTime.now ( ) );
        return otherService;
    }

    @Override
    @Transactional
    public OtherService createOtherService(OtherServiceRequest otherServiceRequest,
                                           String userId,
                                           String userRole,
                                           String autoAddaId) {

        OtherService otherService = mapToEntity ( otherServiceRequest );

        // Set location
        Location location = locationService.createLocation ( otherServiceRequest.getCompanyLocation ( ) );
        otherService.setCompanyLocation ( location );
        otherService.setCompanyOwner ( Integer.parseInt ( autoAddaId ) );

        // Handle company logo
        MediaFile logo = mediaFileRepository.findById ( otherServiceRequest.getLogoId ( ) ).orElseThrow ( () -> new EntityNotFoundException ( "Media file not found with id: " + otherServiceRequest.getLogoId ( ) ) );
        otherService.setCompanyLogo ( logo );

        // Handle VAT certificates
        if ( otherServiceRequest.getVatCertificateIds ( ) != null && ! otherServiceRequest.getVatCertificateIds ( ).isEmpty ( ) ) {
            otherServiceRequest.getVatCertificateIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id )
                        .orElseThrow ( () -> new EntityNotFoundException ( "Media file not found with id: " + id ) );
                otherService.getVatCertificate ( ).add ( mediaFile );
            } );
        }

        // Handle trade license attachments
        if ( otherServiceRequest.getTradeLicenseAttachmentIds ( ) != null && ! otherServiceRequest.getTradeLicenseAttachmentIds ( ).isEmpty ( ) ) {
            otherServiceRequest.getTradeLicenseAttachmentIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id )
                        .orElseThrow ( () -> new EntityNotFoundException ( "Media file not found with id: " + id ) );
                otherService.getTradeLicenseAttachment ( ).add ( mediaFile );
            } );
        }


        // Handle company images
        if ( otherServiceRequest.getCompanyImagesIds ( ) != null && ! otherServiceRequest.getCompanyImagesIds ( ).isEmpty ( ) ) {
            otherServiceRequest.getCompanyImagesIds ( ).forEach ( id -> {
                MediaFile mediaFile = mediaFileRepository.findById ( id )
                        .orElseThrow ( () -> new EntityNotFoundException ( "Media file not found with id: " + id ) );
                otherService.getCompanyImages ( ).add ( mediaFile );
            } );
        }

        return otherServiceRepository.save ( otherService );
    }

    @Override
    public List<OtherService> getAllOtherServices() {
        return otherServiceRepository.findAll ( ).stream ( )
                .filter ( OtherService::isActive )
                .toList ( );
    }

    @Override
    public List<OtherService> getAllOtherServicesByOwnerId(String autoAddaId) {
        return otherServiceRepository.findByCompanyOwner ( Integer.parseInt ( autoAddaId ) ).stream ( )
                .filter ( OtherService::isActive )
                .toList ( );
    }

    @Override
    public Optional<OtherService> getOtherServiceById(Integer id) {
        return otherServiceRepository.findById ( id )
                .filter ( OtherService::isActive );
    }

    @Override
    @Transactional
    public OtherService updateOtherService(Integer id, OtherServiceRequest request) {
        OtherService existing = otherServiceRepository.findById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Other service not found with id: " + id ) );
        // Update location if provided
        if ( request.getCompanyLocation ( ) != null ) {
            Location existingLocation = existing.getCompanyLocation ( );
            Location location = locationService.updateLocation ( existingLocation.getId ( ), request.getCompanyLocation ( ) );
            existing.setCompanyLocation ( location );
        }


        // Update other fields if provided
        if ( request.getServiceType ( ) != null ) {
            existing.setServiceType ( request.getServiceType ( ) );
        }
        if ( request.getCompanyArabicName ( ) != null ) {
            existing.setCompanyArabicName ( request.getCompanyArabicName ( ) );
        }
        if ( request.getCompanyBrandName ( ) != null ) {
            existing.setCompanyBrandName ( request.getCompanyBrandName ( ) );
        }
        if ( request.getOpeningTime ( ) != null ) {
            existing.setOpeningTime ( request.getOpeningTime ( ) );
        }
        if ( request.getClosingTime ( ) != null ) {
            existing.setClosingTime ( request.getClosingTime ( ) );
        }
        if ( request.getClosingTime ( ) != null ) {
            existing.setClosingTime ( request.getClosingTime ( ) );
        }
        if ( request.getWorkingDays ( ) != null ) {
            existing.setWorkingDays ( request.getWorkingDays ( ) );
        }
        if(request.getTradeLicenceNumber() != null) {
            validateTradeLicenceNumber ( request.getTradeLicenceNumber ( ) );
            existing.setTradeLicenceNumber ( request.getTradeLicenceNumber ( ) );
        }
        if ( request.getTrnNumber ( ) != null ) {
            existing.setTrnNumber ( request.getTrnNumber ( ) );
        }
        if ( request.getPhoneNumber ( ) != null ) {
            existing.setPhoneNumber ( request.getPhoneNumber ( ) );
        }
        if ( request.getWebSite ( ) != null ) {
            existing.setWebSite ( request.getWebSite ( ) );
        }
        if ( request.getName ( ) != null ) {
            existing.setName ( request.getName ( ) );
        }
        if ( request.getMobileNumber ( ) != null ) {
            existing.setMobileNumber ( request.getMobileNumber ( ) );
        }
        if ( request.getEmail ( ) != null ) {
            existing.setEmail ( request.getEmail ( ) );
        }


        // Handle company logo update
        if ( request.getLogoId ( ) != null ) {
            // Schedule old logo for cleanup
            if ( existing.getCompanyLogo ( ) != null ) {
                scheduleMediaFileCleanup ( Collections.singletonList ( existing.getCompanyLogo ( ).getId ( ) ) );
            }
            // Upload and set new logo
            MediaFile mediaFile = mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
            existing.setCompanyLogo ( mediaFile );
        }

        // Handle VAT certificate update
        if ( request.getVatCertificateIds ( ) != null ) {
            // Schedule old VAT certificates for cleanup
            List<Long> oldVatCertIds = existing.getVatCertificate ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldVatCertIds.isEmpty ( ) ) {
                scheduleMediaFileCleanup ( oldVatCertIds );
            }

            // Upload and set new VAT certificates
            existing.getVatCertificate ( ).clear ( );
            request.getVatCertificateIds ( ).forEach ( vid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( vid ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getVatCertificate ( ).add ( mediaFile );
            } );

        }

        // Handle trade license update
        if ( request.getTradeLicenseAttachmentIds ( ) != null ) {
            // Schedule old trade licenses for cleanup
            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldTradeLicenseIds.isEmpty ( ) ) {
                scheduleMediaFileCleanup ( oldTradeLicenseIds );
            }

            existing.getTradeLicenseAttachment ( ).clear ( );
            request.getTradeLicenseAttachmentIds ( ).forEach ( tid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( tid ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getTradeLicenseAttachment ( ).add ( mediaFile );
            } );
        }

        // Handle company images update
        if ( request.getCompanyImagesIds ( ) != null ) {
            // Schedule old company images for cleanup
            List<Long> oldCompanyImageIds = existing.getCompanyImages ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldCompanyImageIds.isEmpty ( ) ) {
                scheduleMediaFileCleanup ( oldCompanyImageIds );
            }

            // Upload and set new company images
            existing.getCompanyImages ( ).clear ( );
            request.getCompanyImagesIds ( ).forEach ( cid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( cid ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getCompanyImages ( ).add ( mediaFile );
            } );
        }

        return otherServiceRepository.save ( existing );
    }

    private void validateTradeLicenceNumber(String tradeLicenceNumber) {
        otherServiceRepository.findByTradeLicenceNumber(tradeLicenceNumber).ifPresent(otherService -> {
          throw new AlreadyExistsException ("Other service with trade licence number " + tradeLicenceNumber + " already exists");
        });
    }

    @Override
    @Transactional
    public void deleteOtherService(Integer id) {
        OtherService otherService = otherServiceRepository.findById ( id )
                .orElseThrow ( () -> new EntityNotFoundException ( "Other service not found with id: " + id ) );

        // Mark as inactive instead of deleting
        otherService.setActive ( false );
        otherServiceRepository.save ( otherService );

        // Schedule media files for cleanup
        List<Long> mediaFileIds = new ArrayList<> ( );

        if ( otherService.getCompanyLogo ( ) != null ) {
            mediaFileIds.add ( otherService.getCompanyLogo ( ).getId ( ) );
        }

        mediaFileIds.addAll ( otherService.getVatCertificate ( ).stream ( )
                .map ( MediaFile::getId )
                .toList ( ) );

        mediaFileIds.addAll ( otherService.getTradeLicenseAttachment ( ).stream ( )
                .map ( MediaFile::getId )
                .toList ( ) );

        mediaFileIds.addAll ( otherService.getCompanyImages ( ).stream ( )
                .map ( MediaFile::getId )
                .toList ( ) );

        if ( ! mediaFileIds.isEmpty ( ) ) {
            scheduleMediaFileCleanup ( mediaFileIds );
        }
    }

    @Override
    public List<OtherService> findOtherServicesByBrandName(String name) {
        return otherServiceRepository.findByCompanyBrandNameContainingIgnoreCaseAndIsActiveTrue ( name );
    }

    @Override
    public List<OtherService> findOtherServicesByServiceType(String serviceType) {
        return otherServiceRepository.findByServiceTypeAndIsActiveTrue ( serviceType );
    }

    @Override
    public List<OtherService> findOtherServicesByStatus(Status status) {
        return otherServiceRepository.findByAccountStatusAndIsActiveTrue ( status );
    }


    @Override
    public Optional<OtherService> findOtherServiceByEmail(String email) {
        return otherServiceRepository.findByEmailAndIsActiveTrue ( email );
    }

    @Override
    public Optional<OtherService> findOtherServiceByContact(String contact) {
        return otherServiceRepository.findByPhoneNumberOrMobileNumberAndIsActiveTrue ( contact )
                .stream ( ).findFirst ( );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        for (Long mediaFileId : mediaFileIds) {
            try {
                if ( ! isMediaFileStillReferenced ( mediaFileId ) ) {
                    mediaFileService.delete ( mediaFileId );
                }
            }
            catch ( Exception e ) {
                log.error ( "Error cleaning up media file with id: " + mediaFileId, e );
            }
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check if the media file is still referenced by any other entity
        return otherServiceRepository.countByCompanyLogoId ( mediaFileId ) > 0 ||
                otherServiceRepository.countByVatCertificateId ( mediaFileId ) > 0 ||
                otherServiceRepository.countByTradeLicenseAttachmentId ( mediaFileId ) > 0 ||
                otherServiceRepository.countByCompanyImagesId ( mediaFileId ) > 0;
    }
}
