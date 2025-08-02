package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.config.AuthClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageManagementService {
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final LocationService locationService;
    private final MediaFileRepository mediaFileRepository;
    private final MediaFileService mediaFileService;
    private final AuthClient authClient;

    Garage mapToEntity(GarageRequest garageRequest) {
        Garage garage = new Garage ( );
        garage.setGarageBrandName ( garageRequest.getGarageBrandName ( ) );
        garage.setName ( garageRequest.getName ( ) );
        garage.setEmail ( garageRequest.getEmail ( ) );
        garage.setTradeLicenceNumber ( garageRequest.getTradeLicenceNumber ( ) );
        garage.setOpeningTime ( garageRequest.getOpeningTime ( ) );
        garage.setClosingTime ( garageRequest.getClosingTime ( ) );
        garage.setWorkingDays ( garageRequest.getWorkingDays ( ) );
        garage.setWebSite ( garageRequest.getWebSite ( ) );
        garage.setPhoneNumber ( garageRequest.getPhoneNumber ( ) );
        garage.setMobileNumber ( garageRequest.getMobileNumber ( ) );
        garage.setTrnNumber ( garageRequest.getTrnNumber ( ) );
        garage.setWebSite ( garageRequest.getWebSite ( ) );
        garage.setAccountStatus ( Status.PENDING ); // Default status
        garage.setGarageType ( garageRequest.getGarageType ( ) );
        garage.setChainId ( garageRequest.getChainId ( ) );
        garage.setBrands ( garageRequest.getBrands ( ) );
        garage.setModels ( garageRequest.getModels ( ) );
        garage.setGarageArabicName ( garageRequest.getGarageArabicName ( ) );
        garage.setCreatedAt ( LocalDateTime.now ( ) );
        return garage;
    }

    public Garage createGarage(GarageRequest garageRequest, String userId, String userRole, String autoAddaId) {
        validateTradingLicenseNumber ( garageRequest.getTradeLicenceNumber ( ) );
        Garage garage = mapToEntity ( garageRequest );
        Location location = locationService.createLocation ( garageRequest.getGarageLocation ( ) );
        garage.setGarageLocation ( location );
        garage.setGarageOwner ( Integer.parseInt ( autoAddaId ) );
//      =================================================================================================================
        garage.setGarageLogo ( mediaFileRepository.findById ( garageRequest.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "garage logo not found" ) ) );
        garage.setVatCertificate ( mediaFileRepository.findAllById ( garageRequest.getVatCertificateIds ( ) ) );
        garage.setTradeLicenseAttachment ( mediaFileRepository.findAllById ( garageRequest.getTradeLicenseAttachmentIds ( ) ) );
        garage.setGarageImages ( mediaFileRepository.findAllById ( garageRequest.getGarageImagesIds ( ) ) );
//=====================================================================================================================================================
        return garageRepository.save ( garage );
    }
    private void validateTradingLicenseNumber(String tradeLicenceNumber) {
        garageRepository.findByTradeLicenceNumber(tradeLicenceNumber).ifPresent(garage -> {
            throw new AlreadyExistsException ("A garage with the same trading license number already exists.");
        });
    }


    // Get all garages
    public List<Garage> getAllGarages() {
        return garageRepository.findAll ( ).stream ( ).filter ( Garage::isActive ).toList ( );
    }

    public List<Garage> getAllGaragesByOwnerId(String autoAddaId) {
        return garageRepository.findByGarageOwner ( Integer.parseInt ( autoAddaId ) ).stream ( ).filter ( Garage::isActive ).toList ( );
    }

    // Get all garages with services
    public List<Garage> getAllGaragesWithServices() {
        return garageRepository.findAllWithServices ( );
    }

    // Get garage by ID
    public Optional<Garage> getGarageById(Integer id) {
        return Optional.of ( garageRepository.findById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) ) );
    }

    // Get garage with services
    public Garage getGarageWithServicesById(Integer id) {
        return garageRepository.findWithServicesById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );
    }

    // Update existing garage
    @Transactional
    public Garage updateGarage(Integer id, GarageRequest request) throws IOException {
        Garage existing = garageRepository.findById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );


        if ( request.getGarageLocation ( ) != null ) {
            Location existingGarageLocation = existing.getGarageLocation ( );
            Location location = locationService.updateLocation ( existingGarageLocation.getId ( ), request.getGarageLocation ( ) );
            existing.setGarageLocation ( location );
        }
        if ( request.getGarageType ( ) != null ) {
            existing.setGarageType ( request.getGarageType ( ) );
        }
        if ( request.getBrands ( ) != null ) {
            existing.setBrands ( request.getBrands ( ) );
        }
        if ( request.getModels ( ) != null ) {
            existing.setModels ( request.getModels ( ) );
        }
        if ( request.getGarageArabicName ( ) != null ) {
            existing.setGarageArabicName ( request.getGarageArabicName ( ) );
        }
        if ( request.getTradeLicenceNumber ( ) != null ) {
            validateTradingLicenseNumber ( request.getTradeLicenceNumber ( ) );
            existing.setTradeLicenceNumber ( request.getTradeLicenceNumber ( ) );
        }
        if(request.getOpeningTime() != null) {
            existing.setOpeningTime ( request.getOpeningTime ( ) );
        }
        if(request.getClosingTime() != null) {
            existing.setClosingTime ( request.getClosingTime ( ) );
        }
        if(request.getWorkingDays() != null) {
            existing.setWorkingDays ( request.getWorkingDays ( ) );
        }
        if ( request.getChainId ( ) != null ) {
            existing.setChainId ( request.getChainId ( ) );
        }
        if ( request.getGarageBrandName ( ) != null ) {
            existing.setGarageBrandName ( request.getGarageBrandName ( ) );
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

        List<Long> filesToCleanup = new ArrayList<> ( );

        // Handle garage logo update
        if ( request.getLogoId ( ) != null ) {
            Long prevLogoId = existing.getGarageLogo ( ) != null ? existing.getGarageLogo ( ).getId ( ) : null;
            MediaFile mediaFile = mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
            existing.setGarageLogo ( mediaFile );
            // Schedule old logo for cleanup if changed
            if ( prevLogoId != null && ! prevLogoId.equals ( mediaFile.getId ( ) ) ) {
                filesToCleanup.add ( prevLogoId );
            }

        }


        if ( request.getTradeLicenseAttachmentIds ( ) != null && ! request.getTradeLicenseAttachmentIds ( ).isEmpty ( ) ) {

            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldTradeLicenseIds.isEmpty()) {
                filesToCleanup.addAll(oldTradeLicenseIds);
            }

            // Create new trade license collection
            List<MediaFile> newTradeLicenseAttachments = new ArrayList<>();
            request.getTradeLicenseAttachmentIds ().forEach(tid -> {
                MediaFile mediaFile = mediaFileRepository.findById(tid)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                newTradeLicenseAttachments.add(mediaFile);
            });
            existing.setTradeLicenseAttachment(newTradeLicenseAttachments);
        }


        if ( request.getGarageImagesIds ( ) != null && ! request.getGarageImagesIds ( ).isEmpty ( ) ) {
            // Schedule old garage images for cleanup
            List<Long> oldGarageImageIds = existing.getGarageImages().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldGarageImageIds.isEmpty()) {
                filesToCleanup.addAll(oldGarageImageIds);
            }

            // Create new garage images collection
            List<MediaFile> newGarageImages = new ArrayList<>();
            request.getGarageImagesIds().forEach(gid -> {
                MediaFile mediaFile = mediaFileRepository.findById(gid)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                newGarageImages.add(mediaFile);
            });
            existing.setGarageImages(newGarageImages);
        }

        if ( request.getVatCertificateIds ( ) != null && ! request.getVatCertificateIds ( ).isEmpty ( ) ) {
            // Schedule old VAT certificates for cleanup
            List<Long> oldVatCertIds = existing.getVatCertificate().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldVatCertIds.isEmpty()) {
                filesToCleanup.addAll(oldVatCertIds);
            }

            // Create new VAT certificates collection
            List<MediaFile> newVatCertificates = new ArrayList<>();
            request.getVatCertificateIds().forEach(vid -> {
                MediaFile mediaFile = mediaFileRepository.findById(vid)
                        .orElseThrow(() -> new ResourceNotFoundException("File not found"));
                newVatCertificates.add(mediaFile);
            });
            existing.setVatCertificate(newVatCertificates);
        }

        Garage save = garageRepository.save ( existing );

        if ( ! filesToCleanup.isEmpty ( ) ) {
            scheduleMediaFileCleanup ( filesToCleanup );
        }

        return save;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
        // Small delay to ensure transaction is committed
        try {
            Thread.sleep ( 3000 );

            for (Long fileId : mediaFileIds) {
                try {
                    // Check if file is still referenced elsewhere before deleting
                    if ( ! isMediaFileStillReferenced ( fileId ) ) {
                        mediaFileRepository.deleteById ( fileId );
                        log.info ( "Successfully cleaned up media file: {}", fileId );
                    } else {
                        log.info ( "Media file {} still referenced, skipping cleanup", fileId );
                    }
                }
                catch ( Exception e ) {
                    log.warn ( "Failed to cleanup media file {}: {}", fileId, e.getMessage ( ) );
                }
            }
        }
        catch ( InterruptedException e ) {
            Thread.currentThread ( ).interrupt ( );
            log.warn ( "Media file cleanup interrupted" );
        }
    }

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check if this media file is still referenced by any garage
        // This is a safety check to prevent deleting files that might still be in use
        long garageLogoCount = garageRepository.countByGarageLogoId(mediaFileId);
        long garageImageCount = garageRepository.countByGarageImageId(mediaFileId);
        long garageTradeLicenseCount = garageRepository.countByTradeLicenseId(mediaFileId);
        long garageVatCertCount = garageRepository.countByVatCertificateId(mediaFileId);
        long totalReferences = garageLogoCount + garageImageCount + garageTradeLicenseCount + garageVatCertCount ;
        return totalReferences > 0;
    }

    // Delete garage
    public void deleteGarage(Integer id) {
        Garage garage = garageRepository.findById ( id ).orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );
        garage.setActive ( false );
        garageRepository.save ( garage );
    }

    // Search/filter methods
    public List<Garage> findGaragesByLegalName(String legalName) {
        return garageRepository.findByGarageBrandNameContainingIgnoreCase ( legalName );
    }

    public List<Garage> findGaragesByGarageName(String name) {
        return garageRepository.findByNameContainingIgnoreCase ( name );
    }

    public List<Garage> findGaragesByStatus(Status status) {
        return garageRepository.findByAccountStatus ( status );
    }

    public List<Garage> findGaragesByChainId(Integer chainId) {
        return garageRepository.findByChainId ( chainId );
    }

    public List<Garage> findGaragesByType(String type) {
        return garageRepository.findByGarageType ( type );
    }

    public Optional<Garage> findGarageByEmail(String email) {
        return garageRepository.findByEmail ( email );
    }

    public Optional<Garage> findGarageByContact(String contact) {
        return garageRepository.findByPhoneNumber ( contact );
    }

    public Optional<Garage> findGarageByWebsite(String website) {
        return garageRepository.findByWebSite ( website );
    }

    public Optional<Garage> findGarageByGarageNameExact(String garageName) {
        return garageRepository.findByName ( garageName );
    }

    public List<Garage> findGaragesOfferingAvailableService(Integer serviceId) {
        return garageRepository.findGaragesByAvailableServiceId ( serviceId );
    }

    public Optional<Garage> findGarageByIdAndStatus(Integer id, Status status) {
        return garageRepository.findByIdAndAccountStatus ( id, status );
    }

    public Optional<Garage> findGarageByIdAndChainId(Integer id, Integer chainId) {
        return garageRepository.findByIdAndChainId ( id, chainId );
    }

    public Optional<Garage> findGarageByIdAndGarageType(Integer id, String garageType) {
        return garageRepository.findByIdAndGarageType ( id, garageType );
    }

    public Optional<Garage> findGarageByIdAndLegalName(Integer id, String legalName) {
        return garageRepository.findByIdAndGarageBrandName ( id, legalName );
    }

    public Optional<Garage> findGarageByIdAndWebsite(Integer id, String website) {
        return garageRepository.findByIdAndWebSite ( id, website );
    }

    public Optional<Garage> findGarageByIdAndEmail(Integer id, String email) {
        return garageRepository.findByIdAndEmail ( id, email );
    }

    public Optional<Garage> findGarageByIdAndContact(Integer id, String contactNumber) {
        return garageRepository.findByIdAndPhoneNumber ( id, contactNumber );
    }

    public Optional<Garage> findGarageByIdAndGarageName(Integer id, String name) {
        return garageRepository.findByIdAndName ( id, name );
    }

    public Optional<Garage> findGarageByIdStatusChain(Integer id, Status status, Integer chainId) {
        return garageRepository.findByIdAndAccountStatusAndChainId ( id, status, chainId );
    }

    public Optional<Garage> findGarageByIdStatusType(Integer id, Status status, String type) {
        return garageRepository.findByIdAndAccountStatusAndGarageType ( id, status, type );
    }

    public Optional<Garage> findGarageByIdStatusLegalName(Integer id, Status status, String legalName) {
        return garageRepository.findByIdAndAccountStatusAndGarageBrandName ( id, status, legalName );
    }

    public Technician onboardTechnician(Integer garageId, Technician technician, Integer ownerId)
            throws AccessDeniedException {
        Garage garage = garageRepository.findById ( garageId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Garage not found" ) );

        if ( ! garage.getGarageOwner ( ).equals ( ownerId ) ) {
            throw new AccessDeniedException ( "Only garage owner can onboard technicians" );
        }

        // You may check for duplicate email, phone etc.
        technician.setStatus ( Status.ACTIVE );
        technician.setGarage ( garage );

        return technicianRepository.save ( technician );
    }

}
