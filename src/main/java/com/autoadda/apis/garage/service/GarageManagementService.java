package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.config.AuthClient;
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
import java.util.*;

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

        if ( logoMediaFile != null ) {

        List<MediaFile> vatCertificates = mediaFileService.upload ( vatCertificate );

        garage.setVatCertificate ( vatCertificates );
        }
        if ( tradeLicenseAttachment != null ) {
            List<MediaFile> tradeLicenseAttachments = mediaFileService.upload ( tradeLicenseAttachment );

            garage.setTradeLicenseAttachment ( tradeLicenseAttachments );

        }
        if (tradeLicenseAttachment != null) {
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
    public Garage updateGarage(Integer id, Map<String,Object> request) throws IOException {
        Garage existing = garageRepository.findById ( id )
                .orElseThrow ( () -> new NoSuchElementException ( "Garage not found with id: " + id ) );
        if(request.containsKey ( "data" )) {
            GarageRequest data = (GarageRequest) request.get ( "data" );
            if(data.getGarageLocation() != null) {
                Location existingGarageLocation = existing.getGarageLocation ( );
                Location location = locationService.updateLocation ( existingGarageLocation.getId ( ),data.getGarageLocation ( ) );
                existing.setGarageLocation ( location );
            }
            if(data.getGarageType ()!=null) {
                existing.setGarageType ( data.getGarageType ( ) );
            }
            if(data.getBrands ()!=null) {
                existing.setBrands ( data.getBrands ( ) );
            }
            if(data.getModels ()!=null) {
                existing.setModels ( data.getModels ( ) );
            }
            if(data.getGarageArabicName ()!=null) {
                existing.setGarageArabicName ( data.getGarageArabicName ( ) );
            }
            if (data.getChainId ()!=null) {
                existing.setChainId ( data.getChainId ( ) );
            }
            if(data.getGarageBrandName ()!=null) {
                existing.setGarageBrandName ( data.getGarageBrandName ( ) );
            }
            if(data.getTrnNumber ()!=null) {
                existing.setTrnNumber ( data.getTrnNumber ( ) );
            }
            if(data.getPhoneNumber ()!=null) {
                existing.setPhoneNumber ( data.getPhoneNumber ( ) );
            }
            if(data.getWebSite ()!=null) {
                existing.setWebSite ( data.getWebSite ( ) );
            }
            if(data.getName ()!=null) {
                existing.setName ( data.getName ( ) );
            }
           if(data.getMobileNumber ()!=null) {
               existing.setMobileNumber ( data.getMobileNumber ( ) );
           }
        }
        if (request.containsKey("garageLogo")) {
                // 1. First upload the new logo
                MultipartFile garageLogo = (MultipartFile) request.get("garageLogo");
                List<MediaFile> logo = mediaFileService.upload(Collections.singletonList(garageLogo));
                if (logo == null || logo.isEmpty()) {
                    throw new RuntimeException("Failed to upload new logo");
                }
                // 2. Only after successful upload, delete the old one
                MediaFile oldLogo = existing.getGarageLogo();
                if (oldLogo != null) {
                        mediaFileService.delete(oldLogo.getId());
                }
                // 3. Update the reference
                existing.setGarageLogo(logo.get(0));
        }
        if(request.containsKey ( "garageImages" )) {
            List<MultipartFile> garageImages = (List<MultipartFile>) request.get ( "garageImages" );
            List<MediaFile> mediaFiles = mediaFileService.upload ( garageImages );
            existing.getGarageImages ( ).clear ( );
            existing.getGarageImages ( ).addAll ( mediaFiles );
        }
        if(request.containsKey ( "tradeLicenseAttachment" )) {
            List<MultipartFile> tradeLicenseAttachment = (List<MultipartFile>) request.get ( "tradeLicenseAttachment" );
            List<MediaFile> mediaFiles = mediaFileService.upload ( tradeLicenseAttachment );
            existing.getTradeLicenseAttachment ( ).clear ( );
            existing.getTradeLicenseAttachment (  ).addAll ( mediaFiles );
        }
        if(request.containsKey ( "vatCertificate" )) {
            List<MultipartFile> vatCertificate = (List<MultipartFile>) request.get ( "vatCertificate" );
            List<MediaFile> mediaFiles = mediaFileService.upload ( vatCertificate );
            existing.getVatCertificate ( ).clear ( );
            existing.getVatCertificate (  ).addAll ( mediaFiles );
        }
        return garageRepository.save ( existing );
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
