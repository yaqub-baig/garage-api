package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GarageManagementService {
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final LocationService locationService;
    private final MediaFileRepository mediaFileRepository;
    private final MediaFileService mediaFileService;

    Garage mapToEntity(GarageRequest garageRequest) {
        Garage garage = new Garage ( );
        garage.setGarageBrandName ( garageRequest.getGarageBrandName ( ) );
        garage.setName ( garageRequest.getName ( ) );
        garage.setEmail ( garageRequest.getEmail ( ) );
        garage.setPhoneNumber ( garageRequest.getPhoneNumber ( ) );
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
        System.out.println("1");
        Garage garage = mapToEntity ( garageRequest );
        System.out.println("2");
        Location location = locationService.createLocation ( garageRequest.getGarageLocation ( ) );
        System.out.println("3");
        garage.setGarageLocation ( location );
//      =================================================================================================================
        System.out.println("4");
        List<MediaFile> logo = mediaFileService.upload ( Collections.singletonList ( garageLogo ) );
        System.out.println("5");
        MediaFile logoMediaFile = logo.get ( 0 );
        System.out.println("6");
        garage.setGarageLogo ( logoMediaFile );
        System.out.println("7");
        if ( logoMediaFile != null ) {
        System.out.println("8");
        List<MediaFile> vatCertificates = mediaFileService.upload ( vatCertificate );
        System.out.println("9");
        garage.setVatCertificate ( vatCertificates );
        }
        if ( tradeLicenseAttachment != null ) {
            List<MediaFile> tradeLicenseAttachments = mediaFileService.upload ( tradeLicenseAttachment );
            System.out.println("10");
            garage.setTradeLicenseAttachment ( tradeLicenseAttachments );
            System.out.println("11");
        }
        if (tradeLicenseAttachment != null) {
            List<MediaFile> garageImagess = mediaFileService.upload ( garageImages );
            System.out.println("12");
            garage.setGarageImages ( garageImagess);
            System.out.println("13");
        }
//=====================================================================================================================================================
        return garageRepository.save ( garage );
    }


    // Get all garages
    public List<Garage> getAllGarages() {
        return garageRepository.findAll ( );
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
    public Garage updateGarage(Integer id, GarageRequest updatedGarage) {
        Garage existing = garageRepository.findById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );
//        existing.setGarageBrandName ( updatedGarage.getLegalName ( ) );
//        existing.setName ( updatedGarage.getGarageName ( ) );
//        existing.setEmail ( updatedGarage.getEmailId ( ) );
//        existing.setPhoneNumber ( updatedGarage.getContactNumber ( ) );
//        existing.setAccountStatus ( updatedGarage.getAccountStatus ( ) );
        existing.setWebSite ( updatedGarage.getWebSite ( ) );
        existing.setGarageType ( updatedGarage.getGarageType ( ) );
        existing.setChainId ( updatedGarage.getChainId ( ) );
        return garageRepository.save ( existing );
    }

    // Delete garage
    public void deleteGarage(Integer id) {
        garageRepository.deleteById ( id );
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

        if ( ! garage.getGarageOwner ( ).getId ( ).equals ( ownerId ) ) {
            throw new AccessDeniedException ( "Only garage owner can onboard technicians" );
        }

        // You may check for duplicate email, phone etc.
        technician.setStatus ( Status.ACTIVE );
        technician.setGarage ( garage );

        return technicianRepository.save ( technician );
    }

}
