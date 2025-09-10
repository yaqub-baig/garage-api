package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.GarageMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.request.GarageServiceRequest;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.ERole;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.lang.Boolean.TRUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageManagementService {
    private final GarageWorkingHoursRepository workingHoursRepository;
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final LocationService locationService;
    private final MediaFileRepository mediaFileRepository;
    private final GarageBreakTimeRepository garageBreakTimeRepository;
    private final PickupDropRequestRepository pickupDropRequestRepository;
    private final GarageServiceManagementService garageServiceManagementService;
    private final AvailableServiceRepository availableServiceRepository;
    private final SlotGenerationService slotGenerationService;
    private final GarageMapper garageMapper;
    private final AuthClient authClient;
    private final TechnicianService technicianService;
    Garage mapToEntity(GarageRequest garageRequest) {
        Garage garage = new Garage ( );
        log.info ( "------>" + garageRequest.getBreakStartTime ( ).format ( DateTimeFormatter.ofPattern ( "HH:mm" ) ) );
        log.info ( "------>" + garageRequest.getBreakEndTime ( ).format ( DateTimeFormatter.ofPattern ( "HH:mm" ) ) );
        if ( garageRequest.getGarageBrandName ( ) != null )
            garage.setGarageBrandName ( garageRequest.getGarageBrandName ( ) );

        if(garageRequest.getBreakStartTime ()!=null){
            garage.setBreakStartTime(garageRequest.getBreakStartTime().toString());
        }
        if(garageRequest.getBreakEndTime ()!=null){
            garage.setBreakEndTime(garageRequest.getBreakEndTime().toString());
        }

        if ( garageRequest.getName ( ) != null )
            garage.setName ( garageRequest.getName ( ) );

        if ( garageRequest.getEmail ( ) != null )
            garage.setEmail ( garageRequest.getEmail ( ) );

        if ( garageRequest.getTradeLicenceNumber ( ) != null )
            garage.setTradeLicenceNumber ( garageRequest.getTradeLicenceNumber ( ) );

        if ( garageRequest.getOpeningTime ( ) != null )
            garage.setOpeningTime ( garageRequest.getOpeningTime ( ).toString ( ) );

        if ( garageRequest.getClosingTime ( ) != null )
            garage.setClosingTime ( garageRequest.getClosingTime ( ).toString ( ) );

        if ( garageRequest.getWorkingDays ( ) != null )
            garage.setWorkingDays ( garageRequest.getWorkingDays ( ).toString ( ) );

        if ( garageRequest.getWebSite ( ) != null )
            garage.setWebSite ( garageRequest.getWebSite ( ) );

        if ( garageRequest.getPhoneNumber ( ) != null )
            garage.setPhoneNumber ( garageRequest.getPhoneNumber ( ) );

        if ( garageRequest.getMobileNumber ( ) != null )
            garage.setMobileNumber ( garageRequest.getMobileNumber ( ) );

        if ( garageRequest.getConcurrentAppointmentCapacity ( ) != null )
            garage.setConcurrentAppointmentCapacity ( garageRequest.getConcurrentAppointmentCapacity ( ) );

        if ( garageRequest.getTrnNumber ( ) != null )
            garage.setTrnNumber ( garageRequest.getTrnNumber ( ) );

        if ( garageRequest.getWebSite ( ) != null )
            garage.setWebSite ( garageRequest.getWebSite ( ) );

        garage.setAccountStatus ( Status.PENDING ); // Default status

        if ( garageRequest.getBrands ( ) != null )
            garage.setBrands ( garageRequest.getBrands ( ) );

        if ( garageRequest.getModels ( ) != null )
            garage.setModels ( garageRequest.getModels ( ) );

        if ( garageRequest.getGarageArabicName ( ) != null )
            garage.setGarageArabicName ( garageRequest.getGarageArabicName ( ) );
        garage.setCreatedAt ( LocalDateTime.now ( ) );
        return garage;
    }

    @Transactional
    public GarageResponse createGarage(GarageRequest garageRequest, String autoAddaId) {
        validateGarageOwnerExists(Integer.parseInt ( autoAddaId ));
        Garage garage = mapToEntity ( garageRequest );
        if ( garageRequest.getGarageLocation ( ) != null ) {
            Location location = locationService.createLocation ( garageRequest.getGarageLocation ( ) );
            garage.setGarageLocation ( location );
        }
        garage.setGarageOwner ( Integer.parseInt ( autoAddaId ) );
        if ( garageRequest.getLogoId ( ) != null )
            garage.setGarageLogo ( mediaFileRepository.findById ( garageRequest.getLogoId ( ) )
                    .orElseThrow ( () -> new ResourceNotFoundException ( "garage logo not found" ) ) );


        if ( garageRequest.getVatCertificateIds ( ) != null && ! garageRequest.getVatCertificateIds ( ).isEmpty ( ) )
            garage.setVatCertificate ( mediaFileRepository.findAllById ( garageRequest.getVatCertificateIds ( ) ) );

        if ( garageRequest.getTradeLicenseAttachmentIds ( ) != null && ! garageRequest.getTradeLicenseAttachmentIds ( ).isEmpty ( ) )
            garage.setTradeLicenseAttachment ( mediaFileRepository.findAllById ( garageRequest.getTradeLicenseAttachmentIds ( ) ) );

        if ( garageRequest.getGarageImagesIds ( ) != null && ! garageRequest.getGarageImagesIds ( ).isEmpty ( ) )
            garage.setGarageImages ( mediaFileRepository.findAllById ( garageRequest.getGarageImagesIds ( ) ) );
        Garage save = garageRepository.save ( garage );
        List<GarageService> garageServices = new ArrayList<> ( );
        if ( garageRequest.getGarageServiceIds ( ) != null && ! garageRequest.getGarageServiceIds ( ).isEmpty ( ) ) {
            garageRequest.getGarageServiceIds ( ).forEach ( sId -> {
                availableServiceRepository.findById ( sId )
                        .map ( availableService -> garageServiceManagementService.createGarageService (
                                new GarageServiceRequest ( save.getId ( ), availableService.getId ( ), TRUE ) ) ).map ( garageServices::add )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "invalid id for available service" ) );
            } );
        }
        handleGarageWorkingHours ( save, garageRequest.getWorkingDays ( ), garageRequest.getClosingTime ( ), garageRequest.getOpeningTime ( ) );
        handleGarageBreakHours ( save, garageRequest.getWorkingDays ( ), garageRequest.getBreakStartTime ( ), garageRequest.getBreakEndTime ( ) );
        save.setServices ( garageServices );
        Garage saved = garageRepository.save ( save );
        GenericResponse genericResponse = authClient.updateRole ( saved.getGarageOwner ( ), ERole.GARAGE_OWNER );
        if ( genericResponse == null ) {
            throw new ResourceNotFoundException ( "error while updating role" );
        }
        garageRepository.flush ( );
        workingHoursRepository.flush ( );
        garageBreakTimeRepository.flush ( );
        slotGenerationService.generateSlotsForDays ( saved, 60 );
        onboardTechnician ( saved.getId (),new Technician (),saved.getGarageOwner () );
        return garageMapper.entityToResponse ( saved );
    }

    private void validateGarageOwnerExists(int id) {
        UserResponse user = authClient.getUser ( id );
        if ( user == null ) {
            throw new ResourceNotFoundException ( "user not found" );
        }
    }

    private void handleGarageBreakHours(Garage save, List<DayOfWeek> workingDays, LocalTime breakStartTime, LocalTime breakEndTime) {
        workingDays.forEach ( day -> {
            GarageBreakTime garageBreakTime = new GarageBreakTime ( );
            garageBreakTime.setGarage ( save );
            garageBreakTime.setDayOfWeek ( day );
            garageBreakTime.setBreakStart ( breakStartTime );
            garageBreakTime.setBreakEnd ( breakEndTime );
            log.info ( "---------------->" + breakStartTime.toString ( ) );
            log.info ( "---------------->" + breakEndTime.toString ( ) );
            garageBreakTimeRepository.save ( garageBreakTime );
        } );

    }

    private void handleGarageWorkingHours(Garage save, List<DayOfWeek> workingDays, LocalTime closingTime, LocalTime openingTime) {
        workingDays.forEach ( day -> {
            GarageWorkingHours garageWorkingHours = new GarageWorkingHours ( );
            garageWorkingHours.setGarage ( save );
            garageWorkingHours.setDayOfWeek ( day );
            garageWorkingHours.setOpenTime ( openingTime );
            garageWorkingHours.setCloseTime ( closingTime );
            workingHoursRepository.save ( garageWorkingHours );
        } );
    }

    private void validateTradingLicenseNumber(String tradeLicenceNumber) {
        garageRepository.findByTradeLicenceNumber ( tradeLicenceNumber ).ifPresent ( garage -> {
            throw new AlreadyExistsException ( "A garage with the same trading license number already exists." );
        } );
    }


    // Get all garages
    @Transactional
    public List<GarageResponse> getAllGarages() {
        return garageRepository.findAll ( ).stream ( ).filter ( Garage::isActive ).map ( garageMapper::entityToResponse ).toList ( );
    }

    public List<GarageResponse> getAllGaragesByOwnerId(String autoAddaId) {
        return garageRepository.findByGarageOwner ( Integer.parseInt ( autoAddaId ) ).stream ( )
                .filter ( Garage::isActive )
                .map ( garageMapper::entityToResponse ).toList ( );
    }

    // Get all garages with services
    @Transactional
    public List<Garage> getAllGaragesWithServices() {
        return garageRepository.findAllWithServices ( );
    }

    // Get garage by ID
    @Transactional
    public Optional<GarageResponse> getGarageById(Integer id) {
        return Optional.of ( garageRepository.findById ( id )
                .map ( garageMapper::entityToResponse )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) ) );
    }

    // Get garage with services
    @Transactional
    public Garage getGarageWithServicesById(Integer id) {
        return garageRepository.findWithServicesById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );
    }

    // Update existing garage
    @Transactional
    public GarageResponse updateGarage(Integer id, GarageRequest request) {
        Garage existing = garageRepository.findById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );


        if ( request.getGarageLocation ( ) != null ) {
            Location existingGarageLocation = existing.getGarageLocation ( );
            Location location = locationService.updateLocation ( existingGarageLocation.getId ( ), request.getGarageLocation ( ) );
            existing.setGarageLocation ( location );
        }
        if ( request.getBrands ( ) != null ) {
            existing.setBrands ( request.getBrands ( ) );
        }

        if ( request.getConcurrentAppointmentCapacity ( ) != null ) {
            existing.setConcurrentAppointmentCapacity ( request.getConcurrentAppointmentCapacity ( ) );
            slotGenerationService.updateUpcomingSlotsForGarage ( existing );
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
        if ( request.getOpeningTime ( ) != null ) {
            existing.setOpeningTime ( request.getOpeningTime ( ).toString ( ) );
        }
        if ( request.getClosingTime ( ) != null ) {
            existing.setClosingTime ( request.getClosingTime ( ).toString ( ) );
        }
        if ( request.getWorkingDays ( ) != null ) {
            existing.setWorkingDays ( request.getWorkingDays ( ).toString ( ) );
        }
        if ( request.getBreakStartTime ( ) != null ) {
            existing.setBreakStartTime ( request.getBreakStartTime ( ).toString ( ) );
        }
        if ( request.getBreakEndTime ( ) != null ) {
            existing.setBreakEndTime ( request.getBreakEndTime ( ).toString ( ) );
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
        handleGarageLogo ( request, existing, filesToCleanup );
        handleTradeLicense ( request, existing, filesToCleanup );
        handleGarageImages ( request, existing, filesToCleanup );
        handleVatCertificates ( request, existing, filesToCleanup );
        handleGarageServices ( request, existing );
        Garage save = garageRepository.save ( existing );

        if ( ! filesToCleanup.isEmpty ( ) ) {
            scheduleMediaFileCleanup ( filesToCleanup );
        }

        return garageMapper.entityToResponse ( save );
    }

    private void handleGarageServices(GarageRequest request, Garage existing) {
        if ( request.getGarageServiceIds ( ) != null && ! request.getGarageServiceIds ( ).isEmpty ( ) ) {
            existing.getServices ( ).clear ( );
            request.getGarageServiceIds ( ).forEach ( id -> {
                garageServiceManagementService.getGarageServiceById ( id )
                        .map ( (GarageService gs) -> existing.getServices ( ).add ( gs ) )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "Service not found" ) );
            } );
        }

    }

    private void handleVatCertificates(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if ( request.getVatCertificateIds ( ) != null && ! request.getVatCertificateIds ( ).isEmpty ( ) ) {
            // Schedule old VAT certificates for cleanup
            List<Long> oldVatCertIds = existing.getVatCertificate ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldVatCertIds.isEmpty ( ) ) {
                filesToCleanup.addAll ( oldVatCertIds );
            }

            // Create new VAT certificates collection
            List<MediaFile> newVatCertificates = new ArrayList<> ( );
            request.getVatCertificateIds ( ).forEach ( vid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( vid )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                newVatCertificates.add ( mediaFile );
            } );
            existing.setVatCertificate ( newVatCertificates );
        }
    }

    private void handleGarageImages(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if ( request.getGarageImagesIds ( ) != null && ! request.getGarageImagesIds ( ).isEmpty ( ) ) {
            // Schedule old garage images for cleanup
            List<Long> oldGarageImageIds = existing.getGarageImages ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldGarageImageIds.isEmpty ( ) ) {
                filesToCleanup.addAll ( oldGarageImageIds );
            }

            // Create new garage images collection
            List<MediaFile> newGarageImages = new ArrayList<> ( );
            request.getGarageImagesIds ( ).forEach ( gid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( gid )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                newGarageImages.add ( mediaFile );
            } );
            existing.setGarageImages ( newGarageImages );
        }
    }

    private void handleTradeLicense(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if ( request.getTradeLicenseAttachmentIds ( ) != null && ! request.getTradeLicenseAttachmentIds ( ).isEmpty ( ) ) {

            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment ( ).stream ( )
                    .map ( MediaFile::getId )
                    .toList ( );
            if ( ! oldTradeLicenseIds.isEmpty ( ) ) {
                filesToCleanup.addAll ( oldTradeLicenseIds );
            }

            // Create new trade license collection
            List<MediaFile> newTradeLicenseAttachments = new ArrayList<> ( );
            request.getTradeLicenseAttachmentIds ( ).forEach ( tid -> {
                MediaFile mediaFile = mediaFileRepository.findById ( tid )
                        .orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
                newTradeLicenseAttachments.add ( mediaFile );
            } );
            existing.setTradeLicenseAttachment ( newTradeLicenseAttachments );
        }
    }

    private void handleGarageLogo(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if ( request.getLogoId ( ) != null ) {
            Long prevLogoId = existing.getGarageLogo ( ) != null ? existing.getGarageLogo ( ).getId ( ) : null;
            MediaFile mediaFile = mediaFileRepository.findById ( request.getLogoId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "File not found" ) );
            existing.setGarageLogo ( mediaFile );
            // Schedule old logo for cleanup if changed
            if ( prevLogoId != null && ! prevLogoId.equals ( mediaFile.getId ( ) ) ) {
                filesToCleanup.add ( prevLogoId );
            }

        }
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
        long garageLogoCount = garageRepository.countByGarageLogoId ( mediaFileId );
        long garageImageCount = garageRepository.countByGarageImageId ( mediaFileId );
        long garageTradeLicenseCount = garageRepository.countByTradeLicenseId ( mediaFileId );
        long garageVatCertCount = garageRepository.countByVatCertificateId ( mediaFileId );
        long totalReferences = garageLogoCount + garageImageCount + garageTradeLicenseCount + garageVatCertCount;
        return totalReferences > 0;
    }

    // Delete garage
    @Transactional
    public void deleteGarage(Integer id) {
        Garage garage = garageRepository.findById ( id ).orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );
        garage.setActive ( false );
        garageRepository.save ( garage );
    }

    // Search/filter methods
    @Transactional
    public List<GarageResponse> findGaragesByLegalName(String legalName) {
        return garageRepository.findByGarageBrandNameContainingIgnoreCase ( legalName )
                .stream ( ).map ( garageMapper::entityToResponse )
                .toList ( );
    }

    @Transactional
    public List<Garage> findGaragesByGarageName(String name) {
        return garageRepository.findByNameContainingIgnoreCase ( name );
    }

    @Transactional
    public List<Garage> findGaragesByStatus(Status status) {
        return garageRepository.findByAccountStatus ( status );
    }

//	@Transactional
//    public List<Garage> findGaragesByChainId(Integer chainId) {
//        return garageRepository.findByChainId ( chainId );
//    }

//    @Transactional
//    public List<Garage> findGaragesByType(String type) {
//        return garageRepository.findByGarageType ( type );
//    }

    @Transactional
    public Optional<Garage> findGarageByEmail(String email) {
        return garageRepository.findByEmail ( email );
    }

    @Transactional
    public Optional<Garage> findGarageByContact(String contact) {
        return garageRepository.findByPhoneNumber ( contact );
    }

    @Transactional
    public Optional<Garage> findGarageByWebsite(String website) {
        return garageRepository.findByWebSite ( website );
    }

    @Transactional
    public Optional<Garage> findGarageByGarageNameExact(String garageName) {
        return garageRepository.findByName ( garageName );
    }

    @Transactional
    public List<GarageResponse> findGaragesOfferingAvailableService(Integer serviceId) {
        return garageRepository.findGaragesByAvailableServiceId ( serviceId ).stream ( ).map ( garageMapper::entityToResponse ).toList ( );
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndStatus(Integer id, Status status) {
        return garageRepository.findByIdAndAccountStatus ( id, status );
    }


    @Transactional
    public Optional<Garage> findGarageByIdAndLegalName(Integer id, String legalName) {
        return garageRepository.findByIdAndGarageBrandName ( id, legalName );
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndWebsite(Integer id, String website) {
        return garageRepository.findByIdAndWebSite ( id, website );
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndEmail(Integer id, String email) {
        return garageRepository.findByIdAndEmail ( id, email );
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndContact(Integer id, String contactNumber) {
        return garageRepository.findByIdAndPhoneNumber ( id, contactNumber );
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndGarageName(Integer id, String name) {
        return garageRepository.findByIdAndName ( id, name );
    }


    @Transactional
    public Optional<Garage> findGarageByIdStatusLegalName(Integer id, Status status, String legalName) {
        return garageRepository.findByIdAndAccountStatusAndGarageBrandName ( id, status, legalName );
    }

    @Transactional
    public Technician onboardTechnician(Integer garageId, Technician technician, Integer ownerId)
             {
        Garage garage = garageRepository.findById ( garageId )
                .orElseThrow ( () -> new EntityNotFoundException ( "Garage not found" ) );
        technician.setAutoAddaId ( ownerId );
        // You may check for duplicate email, phone etc.
        technician.setStatus ( Status.ACTIVE );
        technician.setGarage ( garage );
        technician.setEmployeeRole ( EMPLOYEE_ROLE.OWNER );
        return technicianRepository.save ( technician );
    }

    @Transactional
    public PickupDropRequest createPickupDropRequest(PickupDropRequest request) {
        request.setStatus ( Status.REQUESTED );
        return pickupDropRequestRepository.save ( request );
    }

    @Transactional
    public PickupDropRequest assignDriver(Integer requestId, Integer driverId) {
        PickupDropRequest request = pickupDropRequestRepository.findById ( requestId )
                .orElseThrow ( () -> new RuntimeException ( "Request not found" ) );
        request.setAssignedDriverId ( driverId );
        request.setStatus ( Status.SCHEDULED );
        return pickupDropRequestRepository.save ( request );
    }

    @Transactional
    public PickupDropRequest updatePickupDropStatus(Integer requestId, Status status) {
        PickupDropRequest request = pickupDropRequestRepository.findById ( requestId )
                .orElseThrow ( () -> new RuntimeException ( "Request not found" ) );
        request.setStatus ( status );
        return pickupDropRequestRepository.save ( request );
    }

    @Transactional(readOnly = true)
    public List<GarageResponse> searchGarages(String brand, String city, String zip, String garageName, List<Integer> availableServiceIds, String model) {

        Set<Garage> garages = garageRepository.findAllWithLocationAndServices ( );
        List<Garage> searchResults = new ArrayList<> ( List.of ( ) );
        for (Garage garage : garages) {
            boolean cityCheck = true;
            boolean nameCheck = true;
            boolean zipCheck = true;
            boolean brandCheck = true;
            boolean serviceCheck = true;
            boolean modelCheck = true;
            log.info ( " 2  enabledGarages -> " + garages.size ( ) );
            log.info ( "   currentGarage -> " + garage.getName ( ) );

            Location address = garage.getGarageLocation ( );
            log.info ( " 4 enabledGarages -> " );
            if ( StringUtils.isNotEmpty ( city ) ) {
                log.info ( " 5 enabledGarages -> " );
                if ( ! address.getCity ( ).toLowerCase ( ).equalsIgnoreCase ( city.toLowerCase ( ) ) ) {
                    cityCheck = false;
                }
            }

            if ( StringUtils.isNotEmpty ( zip ) ) {
                log.info ( " 6 enabledGarages -> " );
                if ( ! address.getPostalCode ( ).toLowerCase ( ).equalsIgnoreCase ( zip.toLowerCase ( ) ) ) {
                    zipCheck = false;
                }
            }

            if ( StringUtils.isNotEmpty ( garageName ) ) {
                if ( ! garage.getName ( ).toLowerCase ( ).contains ( garageName.toLowerCase ( ) ) ) {
                    nameCheck = false;
                }
            }

            if ( StringUtils.isNotEmpty ( brand ) ) {
                if ( ! garage.getBrands ( ).toLowerCase ( ).contains ( brand.toLowerCase ( ) ) ) {
                    brandCheck = false;
                }
            }
            if ( StringUtils.isNotEmpty ( model ) ) {
                if ( ! garage.getModels ( ).toLowerCase ( ).contains ( model.toLowerCase ( ) ) ) {
                    modelCheck = false;
                }
            }

            if ( availableServiceIds != null && ! availableServiceIds.isEmpty ( ) ) {

                List<Integer> garageServiceIds = garage.getServices ( ).stream ( )
                        .map ( GarageService::getAvailableService ).map ( AvailableService::getId )
                        .toList ( );

                serviceCheck = availableServiceIds.stream ( )
                        .anyMatch ( garageServiceIds::contains );
            }

            if ( cityCheck && nameCheck && zipCheck && brandCheck && serviceCheck && modelCheck ) {
                searchResults.add ( garage );
            }
        }
        return searchResults.stream ( ).map ( garageMapper::entityToResponse ).toList ( );
    }

    @Transactional(readOnly = true)
    public List<Garage> searchGarages2(String brand, String city, String zip, String garageName, List<Integer> availableServiceIds) {


//         Handle null service IDs
        availableServiceIds = availableServiceIds != null ? availableServiceIds : Collections.emptyList ( );

        // Convert search terms to lowercase once
        final String brandTerm = brand != null ? brand.toLowerCase ( ) : "";
        final String cityTerm = city != null ? city.toLowerCase ( ) : "";
        final String zipTerm = zip != null ? zip.toLowerCase ( ) : "";
        final String garageNameTerm = garageName != null ? garageName.toLowerCase ( ) : "";
        final List<Integer> serviceIds = new ArrayList<> ( availableServiceIds );

        // Fetch all necessary data in a single query with joins
        Set<Garage> garages = garageRepository.findAllWithLocationAndServices ( );


        return garages.stream ( )
                .filter ( Objects::nonNull )
                .filter ( garage -> matchesBrand ( garage, brandTerm ) )
                .filter ( garage -> matchesCity ( garage, cityTerm ) )
                .filter ( garage -> matchesZip ( garage, zipTerm ) )
                .filter ( garage -> matchesName ( garage, garageNameTerm ) )
                .filter ( garage -> matchesServices ( garage, serviceIds ) )
                .toList ( );
    }

    private boolean matchesBrand(Garage garage, String brandTerm) {
        return brandTerm.isEmpty ( ) ||
                (garage.getGarageBrandName ( ) != null &&
                        garage.getGarageBrandName ( ).toLowerCase ( ).contains ( brandTerm ));
    }

    private boolean matchesCity(Garage garage, String cityTerm) {
        return cityTerm.isEmpty ( ) ||
                (garage.getGarageLocation ( ) != null &&
                        garage.getGarageLocation ( ).getCity ( ) != null &&
                        garage.getGarageLocation ( ).getCity ( ).toLowerCase ( ).contains ( cityTerm ));
    }

    private boolean matchesZip(Garage garage, String zipTerm) {
        return zipTerm.isEmpty ( ) ||
                (garage.getGarageLocation ( ) != null &&
                        garage.getGarageLocation ( ).getArea ( ) != null &&
                        garage.getGarageLocation ( ).getArea ( ).toLowerCase ( ).contains ( zipTerm ));
    }

    private boolean matchesName(Garage garage, String nameTerm) {
        return nameTerm.isEmpty ( ) ||
                (garage.getName ( ) != null &&
                        garage.getName ( ).toLowerCase ( ).contains ( nameTerm ));
    }

    private boolean matchesServices(Garage garage, List<Integer> serviceIds) {
        if ( serviceIds.isEmpty ( ) ) {
            return true;
        }
        if ( garage.getServices ( ) == null || garage.getServices ( ).isEmpty ( ) ) {
            return false;
        }
        return garage.getServices ( ).stream ( )
                .map ( GarageService::getAvailableService )
                .map ( AvailableService::getId )
                .anyMatch ( serviceIds::contains );
    }

}