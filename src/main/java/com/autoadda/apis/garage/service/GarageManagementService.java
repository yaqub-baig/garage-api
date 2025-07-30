package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.config.AuthClient;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.Technician;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
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

    public Garage createGarage(GarageRequest garageRequest, String userId, String userRole, String autoAddaId, MultipartFile garageLogo, List<MultipartFile> vatCertificate, List<MultipartFile> tradeLicenseAttachment, List<MultipartFile> garageImages) throws IOException {

        Garage garage = mapToEntity ( garageRequest );

        Location location = locationService.createLocation ( garageRequest.getGarageLocation ( ) );

        garage.setGarageLocation ( location );
        garage.setGarageOwner ( Integer.parseInt ( autoAddaId  ));
//      =================================================================================================================

        List<MediaFile> logo = mediaFileService.upload ( Collections.singletonList ( garageLogo ) );

        MediaFile logoMediaFile = logo.get ( 0 );

        garage.setGarageLogo ( logoMediaFile );

        if ( vatCertificate != null ) {

        List<MediaFile> vatCertificates = mediaFileService.upload ( vatCertificate );

        garage.setVatCertificate ( vatCertificates );
        }
        if ( tradeLicenseAttachment != null ) {
            List<MediaFile> tradeLicenseAttachments = mediaFileService.upload ( tradeLicenseAttachment );

            garage.setTradeLicenseAttachment ( tradeLicenseAttachments );

        }
        if (garageImages != null) {
            List<MediaFile> garageImagess = mediaFileService.upload ( garageImages );

            garage.setGarageImages ( garageImagess);

        }
//=====================================================================================================================================================
        return garageRepository.save ( garage );
    }


    // Get all garages
    public List<Garage> getAllGarages() {
        return garageRepository.findAll ( ).stream ().filter ( Garage::isActive ).toList ();
    }

    public List<Garage> getAllGaragesByOwnerId(String autoAddaId) {
        return garageRepository.findByGarageOwner (Integer.parseInt ( autoAddaId ) ).stream ().filter ( Garage::isActive ).toList ();
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


            if(request.getGarageLocation() != null) {
                Location existingGarageLocation = existing.getGarageLocation ( );
                Location location = locationService.updateLocation ( existingGarageLocation.getId ( ),request.getGarageLocation ( ) );
                existing.setGarageLocation ( location );
            }
            if(request.getGarageType ()!=null) {
                existing.setGarageType ( request.getGarageType ( ) );
            }
            if(request.getBrands ()!=null) {
                existing.setBrands ( request.getBrands ( ) );
            }
            if(request.getModels ()!=null) {
                existing.setModels ( request.getModels ( ) );
            }
            if(request.getGarageArabicName ()!=null) {
                existing.setGarageArabicName ( request.getGarageArabicName ( ) );
            }
            if (request.getChainId ()!=null) {
                existing.setChainId ( request.getChainId ( ) );
            }
            if(request.getGarageBrandName ()!=null) {
                existing.setGarageBrandName ( request.getGarageBrandName ( ) );
            }
            if(request.getTrnNumber ()!=null) {
                existing.setTrnNumber ( request.getTrnNumber ( ) );
            }
            if(request.getPhoneNumber ()!=null) {
                existing.setPhoneNumber ( request.getPhoneNumber ( ) );
            }
            if(request.getWebSite ()!=null) {
                existing.setWebSite ( request.getWebSite ( ) );
            }
            if(request.getName ()!=null) {
                existing.setName ( request.getName ( ) );
            }
           if(request.getMobileNumber ()!=null) {
               existing.setMobileNumber ( request.getMobileNumber ( ) );
           }

        List<Long> filesToCleanup = new ArrayList<> ();

        // Handle garage logo update
        if (request.getLogoId ()!=null) {
            Long prevLogoId = existing.getGarageLogo() != null ? existing.getGarageLogo().getId() : null;
            MediaFile mediaFile = mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
            existing.setGarageLogo(mediaFile);
            // Schedule old logo for cleanup if changed
            if (prevLogoId != null && !prevLogoId.equals(mediaFile.getId())) {
                filesToCleanup.add(prevLogoId);
            }

        }


        if(request.getTradeLicenseAttachmentIds ()!=null && !request.getTradeLicenseAttachmentIds ().isEmpty ()) {

            filesToCleanup.addAll ( existing.getTradeLicenseAttachment ( ).stream ().map ( MediaFile::getId ).toList ( ) ) ;
            existing.getTradeLicenseAttachment ( ).clear ( );
            request.getTradeLicenseAttachmentIds ().forEach ( mediaFileId -> {
                MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getTradeLicenseAttachment ( ).add ( mediaFile );
            } );
        }


        if(request.getGarageImagesIds ()!=null && !request.getGarageImagesIds ().isEmpty ()){
            filesToCleanup.addAll ( existing.getGarageImages ( ).stream ().map ( MediaFile::getId ).toList ( ) ) ;
            existing.getGarageImages ( ).clear ( );
            request.getGarageImagesIds ().forEach ( mediaFileId -> {
                MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getGarageImages ( ).add ( mediaFile );
            } );
        }

        if(request.getVatCertificateIds ()!=null && !request.getVatCertificateIds ().isEmpty()) {
            filesToCleanup.addAll ( existing.getVatCertificate ( ).stream ().map ( MediaFile::getId ).toList ( ) ) ;
            existing.getVatCertificate ( ).clear ( );
            request.getVatCertificateIds ().forEach ( mediaFileId -> {
                MediaFile mediaFile = mediaFileRepository.findById ( mediaFileId ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                existing.getVatCertificate ( ).add ( mediaFile );
            } );
        }
        if (!filesToCleanup.isEmpty()) {
            scheduleMediaFileCleanup(filesToCleanup);
        }


        return garageRepository.save ( existing );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleMediaFileCleanup(List<Long> mediaFileIds) {
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

    private boolean isMediaFileStillReferenced(Long mediaFileId) {
        // Check if this media file is still referenced by any garage
        // This is a safety check to prevent deleting files that might still be in use
        long count = garageRepository.countByGarageLogoId(mediaFileId);
        return count > 0;
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
