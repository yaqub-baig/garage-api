package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.exception.UnauthorizedAccessException;
import com.autoadda.apis.garage.mapper.GarageMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.request.GarageServiceRequest;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.ERole;
import com.autoadda.apis.garage.util.GarageVerificationStatus;
import com.autoadda.apis.garage.util.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageManagementService {
    public static final String GARAGE_NOT_FOUND = "Garage not found";
    public static final String GARAGE_NOT_FOUND_WITH_ID = "Garage not found with id: ";
    public static final String FILE_NOT_FOUND = "File not found";
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
    private final MediaFileCleanupService mediaFileCleanupService;

    Garage mapToEntity(GarageRequest garageRequest) {
        Garage garage = new Garage();

        logBreakTimes(garageRequest);
        mapBasicInfo(garageRequest, garage);
        mapContactInfo(garageRequest, garage);
        mapTimeInfo(garageRequest, garage);
        mapBusinessInfo(garageRequest, garage);
        mapVehicleInfo(garageRequest, garage);

        garage.setAccountStatus(GarageVerificationStatus.PENDING);
        garage.setCreatedAt(LocalDateTime.now());
        return garage;
    }

    private void logBreakTimes(GarageRequest garageRequest) {
        Optional.ofNullable(garageRequest.getBreakStartTime())
                .ifPresent(time -> log.info("------>1" + time.format(DateTimeFormatter.ofPattern("HH:mm"))));
        Optional.ofNullable(garageRequest.getBreakEndTime())
                .ifPresent(time -> log.info("------>" + time.format(DateTimeFormatter.ofPattern("HH:mm"))));
    }

    private void mapBasicInfo(GarageRequest request, Garage garage) {
        Optional.ofNullable(request.getGarageBrandName()).ifPresent(garage::setGarageBrandName);
        Optional.ofNullable(request.getName()).ifPresent(garage::setName);
        Optional.ofNullable(request.getGarageArabicName()).ifPresent(garage::setGarageArabicName);
    }

    private void mapContactInfo(GarageRequest request, Garage garage) {
        Optional.ofNullable(request.getEmail()).ifPresent(garage::setEmail);
        Optional.ofNullable(request.getPhoneNumber()).ifPresent(garage::setPhoneNumber);
        Optional.ofNullable(request.getMobileNumber()).ifPresent(garage::setMobileNumber);
        Optional.ofNullable(request.getWebsite()).ifPresent(garage::setWebsite);
    }

    private void mapTimeInfo(GarageRequest request, Garage garage) {
        Optional.ofNullable(request.getOpeningTime())
                .map(LocalTime::toString)
                .ifPresent(garage::setOpeningTime);
        Optional.ofNullable(request.getClosingTime())
                .map(LocalTime::toString)
                .ifPresent(garage::setClosingTime);
        Optional.ofNullable(request.getBreakStartTime())
                .map(LocalTime::toString)
                .ifPresent(garage::setBreakStartTime);
        Optional.ofNullable(request.getBreakEndTime())
                .map(LocalTime::toString)
                .ifPresent(garage::setBreakEndTime);
        Optional.ofNullable(request.getWorkingDays())
                .map(Object::toString)
                .ifPresent(garage::setWorkingDays);
    }

    private void mapBusinessInfo(GarageRequest request, Garage garage) {
        Optional.ofNullable(request.getTradeLicenceNumber()).ifPresent(garage::setTradeLicenceNumber);
        Optional.ofNullable(request.getTrnNumber()).ifPresent(garage::setTrnNumber);
        Optional.ofNullable(request.getConcurrentAppointmentCapacity())
                .ifPresent(garage::setConcurrentAppointmentCapacity);
    }

    private void mapVehicleInfo(GarageRequest request, Garage garage) {
        Optional.ofNullable(request.getBrands()).ifPresent(garage::setBrands);
        Optional.ofNullable(request.getModels()).ifPresent(garage::setModels);
    }

    @Transactional
    public GarageResponse createGarage(GarageRequest garageRequest, String autoAddaId) {
        validateGarageOwnerExists(Integer.parseInt(autoAddaId));
        Garage garage = mapToEntity(garageRequest);
        if (garageRequest.getGarageLocation() != null) {
            Location location = locationService.createLocation(garageRequest.getGarageLocation());
            garage.setGarageLocation(location);
        }
        garage.setGarageOwner(Integer.parseInt(autoAddaId));
        if (garageRequest.getLogoId() != null)
            garage.setGarageLogo(mediaFileRepository.findById(garageRequest.getLogoId())
                    .orElseThrow(() -> new ResourceNotFoundException("garage logo not found")));


        if (garageRequest.getVatCertificateIds() != null && !garageRequest.getVatCertificateIds().isEmpty())
            garage.setVatCertificate(mediaFileRepository.findAllById(garageRequest.getVatCertificateIds()));

        if (garageRequest.getTradeLicenseAttachmentIds() != null && !garageRequest.getTradeLicenseAttachmentIds().isEmpty())
            garage.setTradeLicenseAttachment(mediaFileRepository.findAllById(garageRequest.getTradeLicenseAttachmentIds()));

        if (garageRequest.getGarageImagesIds() != null && !garageRequest.getGarageImagesIds().isEmpty())
            garage.setGarageImages(mediaFileRepository.findAllById(garageRequest.getGarageImagesIds()));
        Garage save = garageRepository.save(garage);
        List<GarageService> garageServices = handleGarageService(garageRequest, save);
        handleGarageWorkingHours(save, garageRequest.getWorkingDays(), garageRequest.getClosingTime(), garageRequest.getOpeningTime());
        handleGarageBreakHours(save, garageRequest.getWorkingDays(), garageRequest.getBreakStartTime(), garageRequest.getBreakEndTime());
        save.setServices(garageServices);
        Garage saved = garageRepository.save(save);
        GenericResponse genericResponse = authClient.updateRole(saved.getGarageOwner(), ERole.GARAGE_OWNER);
        if (genericResponse == null) {
            throw new ResourceNotFoundException("error while updating role");
        }
        garageRepository.flush();
        workingHoursRepository.flush();
        garageBreakTimeRepository.flush();
        slotGenerationService.generateSlotsForDays(saved, 60);
        onboardTechnician(saved.getId(), new Technician(), saved.getGarageOwner());
        return garageMapper.entityToResponse(saved);
    }

    private List<GarageService> handleGarageService(GarageRequest garageRequest, Garage save) {
        List<GarageService> garageServices = new ArrayList<>();
        if (garageRequest.getGarageServiceIds() != null && !garageRequest.getGarageServiceIds().isEmpty()) {
            garageRequest.getGarageServiceIds().forEach(sId ->
                    availableServiceRepository.findById(sId)
                            .map(availableService -> garageServiceManagementService.createGarageService(
                                    new GarageServiceRequest(save.getId(), availableService.getId(), TRUE))).map(garageServices::add)
                            .orElseThrow(() -> new ResourceNotFoundException("invalid id for available service"))
            );
        }
        return garageServices;
    }

    private List<GarageService> handleUpdateGarageService(GarageRequest garageRequest, Garage save) {
        List<GarageService> garageServices = new ArrayList<>();

        if (garageRequest.getGarageServiceIds() != null && !garageRequest.getGarageServiceIds().isEmpty()) {
            // Get existing garage services for this garage
            List<GarageService> existingGarageServices = garageServiceManagementService.getByGarageIdAndActive(save.getId(), TRUE);

            // Extract existing available service IDs
            List<Integer> existingAvailableServiceIds = existingGarageServices.stream()
                    .map(gs -> gs.getAvailableService().getId())
                    .toList();

            // Add existing services to the result list
            garageServices.addAll(existingGarageServices);

            // Filter incoming IDs to only get NEW ones (not already existing)
            List<Integer> newServiceIds = garageRequest.getGarageServiceIds().stream()
                    .filter(sId -> !existingAvailableServiceIds.contains(sId))
                    .toList();

            // Create only NEW garage services
            newServiceIds.forEach(sId ->
                    availableServiceRepository.findById(sId)
                            .map(availableService -> garageServiceManagementService.createGarageService(
                                    new GarageServiceRequest(save.getId(), availableService.getId(), TRUE)))
                            .map(garageServices::add)
                            .orElseThrow(() -> new ResourceNotFoundException("invalid id for available service"))
            );
        }
        return garageServices;
    }

    private void validateGarageOwnerExists(int id) {
        UserResponse user = authClient.getUser(id);
        if (user == null) {
            throw new ResourceNotFoundException("user not found");
        }
    }

    private void handleGarageBreakHours(Garage save, List<DayOfWeek> workingDays, LocalTime breakStartTime, LocalTime breakEndTime) {
        workingDays.forEach(day -> {
            GarageBreakTime garageBreakTime = new GarageBreakTime();
            garageBreakTime.setGarage(save);
            garageBreakTime.setDayOfWeek(day);
            garageBreakTime.setBreakStart(breakStartTime);
            garageBreakTime.setBreakEnd(breakEndTime);
            log.info("---------------->" + breakStartTime.toString());
            log.info("---------------->" + breakEndTime.toString());
            garageBreakTimeRepository.save(garageBreakTime);
        });

    }

    private void handleGarageWorkingHours(Garage save, List<DayOfWeek> workingDays, LocalTime closingTime, LocalTime openingTime) {
        workingDays.forEach(day -> {
            GarageWorkingHours garageWorkingHours = new GarageWorkingHours();
            garageWorkingHours.setGarage(save);
            garageWorkingHours.setDayOfWeek(day);
            garageWorkingHours.setOpenTime(openingTime);
            garageWorkingHours.setCloseTime(closingTime);
            workingHoursRepository.save(garageWorkingHours);
        });
    }

    private void validateTradingLicenseNumber(String tradeLicenceNumber) {
        garageRepository.findByTradeLicenceNumber(tradeLicenceNumber).ifPresent(garage -> {
            throw new AlreadyExistsException("A garage with the same trading license number already exists.");
        });
    }


    // Get all garages
    @Transactional
    public List<GarageResponse> getAllGarages() {
        return garageRepository.findAll().stream().filter(Garage::isActive).map(garageMapper::entityToResponse).toList();
    }

    public List<GarageResponse> getAllGaragesByOwnerId(String autoAddaId) {
        return garageRepository.findByGarageOwner(Integer.parseInt(autoAddaId)).stream()
                .filter(Garage::isActive)
                .map(garageMapper::entityToResponse).toList();
    }

    // Get all garages with services
    @Transactional
    public List<Garage> getAllGaragesWithServices() {
        return garageRepository.findAllWithServices();
    }

    // Get garage by ID
    @Transactional
    public Optional<GarageResponse> getGarageById(Integer id) {
        return Optional.of(garageRepository.findById(id)
                .map(garageMapper::entityToResponse)
                .orElseThrow(() -> new NoSuchElementException(GARAGE_NOT_FOUND_WITH_ID + id)));
    }

    // Get garage with services
    @Transactional
    public Garage getGarageWithServicesById(Integer id) {
        return garageRepository.findWithServicesById(id)
                .orElseThrow(() -> new NoSuchElementException(GARAGE_NOT_FOUND_WITH_ID + id));
    }

    // Update existing garage
    @Transactional
    public GarageResponse updateGarage(Integer id, GarageRequest request) {
        Garage existing = garageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(GARAGE_NOT_FOUND_WITH_ID + id));

        updateGarageLocation(request, existing);
        updateBasicInfo(request, existing);
        updateContactInfo(request, existing);
        updateTimeInfo(request, existing);
        updateBusinessInfo(request, existing);
        updateVehicleInfo(request, existing);

        List<Long> filesToCleanup = new ArrayList<>();
        updateMediaFiles(request, existing, filesToCleanup);
        updateGarageServices(request, existing);

        Garage saved = garageRepository.save(existing);
        cleanupOldMediaFiles(filesToCleanup);

        return garageMapper.entityToResponse(saved);
    }

    private void updateGarageLocation(GarageRequest request, Garage existing) {
        Optional.ofNullable(request.getGarageLocation())
                .ifPresent(locationRequest -> {
                    Location existingLocation = existing.getGarageLocation();
                    Location updated = locationService.updateLocation(existingLocation.getId(), locationRequest);
                    existing.setGarageLocation(updated);
                });
    }

    private void updateBasicInfo(GarageRequest request, Garage existing) {
        Optional.ofNullable(request.getGarageBrandName()).ifPresent(existing::setGarageBrandName);
        Optional.ofNullable(request.getName()).ifPresent(existing::setName);
        Optional.ofNullable(request.getGarageArabicName()).ifPresent(existing::setGarageArabicName);
    }

    private void updateContactInfo(GarageRequest request, Garage existing) {
        Optional.ofNullable(request.getEmail()).ifPresent(existing::setEmail);
        Optional.ofNullable(request.getPhoneNumber()).ifPresent(existing::setPhoneNumber);
        Optional.ofNullable(request.getMobileNumber()).ifPresent(existing::setMobileNumber);
        Optional.ofNullable(request.getWebsite()).ifPresent(existing::setWebsite);
    }

    private void updateTimeInfo(GarageRequest request, Garage existing) {
        Optional.ofNullable(request.getOpeningTime())
                .map(LocalTime::toString)
                .ifPresent(existing::setOpeningTime);
        Optional.ofNullable(request.getClosingTime())
                .map(LocalTime::toString)
                .ifPresent(existing::setClosingTime);
        Optional.ofNullable(request.getBreakStartTime())
                .map(LocalTime::toString)
                .ifPresent(existing::setBreakStartTime);
        Optional.ofNullable(request.getBreakEndTime())
                .map(LocalTime::toString)
                .ifPresent(existing::setBreakEndTime);
        Optional.ofNullable(request.getWorkingDays())
                .map(Object::toString)
                .ifPresent(existing::setWorkingDays);
    }

    private void updateBusinessInfo(GarageRequest request, Garage existing) {
        Optional.ofNullable(request.getTradeLicenceNumber())
                .ifPresent(licenceNumber -> {
                    validateTradingLicenseNumber(licenceNumber);
                    existing.setTradeLicenceNumber(licenceNumber);
                });
        Optional.ofNullable(request.getTrnNumber()).ifPresent(existing::setTrnNumber);
        Optional.ofNullable(request.getConcurrentAppointmentCapacity())
                .ifPresent(capacity -> {
                    existing.setConcurrentAppointmentCapacity(capacity);
                    slotGenerationService.updateUpcomingSlotsForGarage(existing);
                });
    }

    private void updateVehicleInfo(GarageRequest request, Garage existing) {
        Optional.ofNullable(request.getBrands()).ifPresent(existing::setBrands);
        Optional.ofNullable(request.getModels()).ifPresent(existing::setModels);
    }

    private void updateMediaFiles(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        handleGarageLogo(request, existing, filesToCleanup);
        handleTradeLicense(request, existing, filesToCleanup);
        handleGarageImages(request, existing, filesToCleanup);
        handleVatCertificates(request, existing, filesToCleanup);
    }

    private void updateGarageServices(GarageRequest request, Garage existing) {
        List<GarageService> garageServices = handleUpdateGarageService(request, existing);
        existing.setServices(garageServices);
    }

    private void cleanupOldMediaFiles(List<Long> filesToCleanup) {
        if (!filesToCleanup.isEmpty()) {
            mediaFileCleanupService.scheduleCleanup(filesToCleanup);
        }
    }

    private void handleVatCertificates(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if (request.getVatCertificateIds() != null && !request.getVatCertificateIds().isEmpty()) {
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
                        .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND));
                newVatCertificates.add(mediaFile);
            });
            existing.setVatCertificate(newVatCertificates);
        }
    }

    private void handleGarageImages(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if (request.getGarageImagesIds() != null && !request.getGarageImagesIds().isEmpty()) {
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
                        .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND));
                newGarageImages.add(mediaFile);
            });
            existing.setGarageImages(newGarageImages);
        }
    }

    private void handleTradeLicense(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if (request.getTradeLicenseAttachmentIds() != null && !request.getTradeLicenseAttachmentIds().isEmpty()) {

            List<Long> oldTradeLicenseIds = existing.getTradeLicenseAttachment().stream()
                    .map(MediaFile::getId)
                    .toList();
            if (!oldTradeLicenseIds.isEmpty()) {
                filesToCleanup.addAll(oldTradeLicenseIds);
            }

            // Create new trade license collection
            List<MediaFile> newTradeLicenseAttachments = new ArrayList<>();
            request.getTradeLicenseAttachmentIds().forEach(tid -> {
                MediaFile mediaFile = mediaFileRepository.findById(tid)
                        .orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND));
                newTradeLicenseAttachments.add(mediaFile);
            });
            existing.setTradeLicenseAttachment(newTradeLicenseAttachments);
        }
    }

    private void handleGarageLogo(GarageRequest request, Garage existing, List<Long> filesToCleanup) {
        if (request.getLogoId() != null) {
            Long prevLogoId = existing.getGarageLogo() != null ? existing.getGarageLogo().getId() : null;
            MediaFile mediaFile = mediaFileRepository.findById(request.getLogoId()).orElseThrow(() -> new ResourceNotFoundException(FILE_NOT_FOUND));
            existing.setGarageLogo(mediaFile);
            // Schedule old logo for cleanup if changed
            if (prevLogoId != null && !prevLogoId.equals(mediaFile.getId())) {
                filesToCleanup.add(prevLogoId);
            }

        }
    }

    // Delete garage
    @Transactional
    public void deleteGarage(Integer id) {
        Garage garage = garageRepository.findById(id).orElseThrow(() -> new NoSuchElementException(GARAGE_NOT_FOUND_WITH_ID + id));
        garage.setActive(false);
        garageRepository.save(garage);
    }

    // Search/filter methods
    @Transactional
    public List<GarageResponse> findGaragesByLegalName(String legalName) {
        return garageRepository.findByGarageBrandNameContainingIgnoreCaseAndApproved(legalName)
                .stream().map(garageMapper::entityToResponse)
                .toList();
    }

    @Transactional
    public List<Garage> findGaragesByGarageName(String name) {
        return garageRepository.findByNameContainingIgnoreCase(name);
    }

    @Transactional
    public List<Garage> findGaragesByStatus(Status status) {
        return garageRepository.findByAccountStatus(status);
    }


    @Transactional
    public Optional<Garage> findGarageByEmail(String email) {
        return garageRepository.findByEmail(email);
    }

    @Transactional
    public Optional<Garage> findGarageByContact(String contact) {
        return garageRepository.findByPhoneNumber(contact);
    }

    @Transactional
    public Optional<Garage> findGarageByWebsite(String website) {
        return garageRepository.findByWebsite(website);
    }

    @Transactional
    public Optional<Garage> findGarageByGarageNameExact(String garageName) {
        return garageRepository.findByName(garageName);
    }

    @Transactional
    public List<GarageResponse> findGaragesOfferingAvailableService(Integer serviceId) {
        return garageRepository.findGaragesByAvailableServiceId(serviceId).stream().map(garageMapper::entityToResponse).toList();
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndStatus(Integer id, Status status) {
        return garageRepository.findByIdAndAccountStatus(id, status);
    }


    @Transactional
    public Optional<Garage> findGarageByIdAndLegalName(Integer id, String legalName) {
        return garageRepository.findByIdAndGarageBrandName(id, legalName);
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndWebsite(Integer id, String website) {
        return garageRepository.findByIdAndWebsite(id, website);
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndEmail(Integer id, String email) {
        return garageRepository.findByIdAndEmail(id, email);
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndContact(Integer id, String contactNumber) {
        return garageRepository.findByIdAndPhoneNumber(id, contactNumber);
    }

    @Transactional
    public Optional<Garage> findGarageByIdAndGarageName(Integer id, String name) {
        return garageRepository.findByIdAndName(id, name);
    }


    @Transactional
    public Optional<Garage> findGarageByIdStatusLegalName(Integer id, Status status, String legalName) {
        return garageRepository.findByIdAndAccountStatusAndGarageBrandName(id, status, legalName);
    }

    @Transactional
    public Technician onboardTechnician(Integer garageId, Technician technician, Integer ownerId) {
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException(GARAGE_NOT_FOUND));
        UserResponse user = authClient.getUser(ownerId);
        technician.setAutoAddaId(user.getId());
        // You may check for duplicate email, phone etc.
        technician.setFirstName(user.getName());
        technician.setEmailId(user.getEmail());
        technician.setStatus(Status.ACTIVE);
        technician.setGarage(garage);
        technician.setEmployeeRole(EMPLOYEE_ROLE.OWNER);
        return technicianRepository.save(technician);
    }

    @Transactional
    public PickupDropRequest createPickupDropRequest(PickupDropRequest request) {
        request.setStatus(Status.REQUESTED);
        return pickupDropRequestRepository.save(request);
    }

    @Transactional
    public PickupDropRequest assignDriver(Integer requestId, Integer driverId) {
        PickupDropRequest request = pickupDropRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setAssignedDriverId(driverId);
        request.setStatus(Status.SCHEDULED);
        return pickupDropRequestRepository.save(request);
    }

    @Transactional
    public PickupDropRequest updatePickupDropStatus(Integer requestId, Status status) {
        PickupDropRequest request = pickupDropRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        return pickupDropRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<GarageResponse> searchGarages(String brand, String city, String zip, String garageName, List<Integer> availableServiceIds, String model) {
        Set<Garage> garages = garageRepository.findAllWithLocationAndServices();
        log.info("Searching through {} garages with filters - city: {}, zip: {}, name: {}, brand: {}, model: {}, serviceIds: {}",
                garages.size(), city, zip, garageName, brand, model, availableServiceIds);

        List<Garage> searchResults = garages.stream()
                .filter(garage -> matchesSearchCriteria(garage, brand, city, zip, garageName, availableServiceIds, model))
                .toList();

        log.info("Search found {} matching garages", searchResults.size());
        return searchResults.stream().map(garageMapper::entityToResponse).toList();
    }

    /**
     * Checks if a garage matches all provided search criteria.
     * Empty/null criteria are ignored (match all).
     *
     * @return true if garage matches all non-empty criteria
     */
    private boolean matchesSearchCriteria(Garage garage, String brand, String city, String zip,
                                          String garageName, List<Integer> availableServiceIds, String model) {
        return matchesCityFilter(garage, city)
                && matchesZipFilter(garage, zip)
                && matchesNameFilter(garage, garageName)
                && matchesBrandFilter(garage, brand)
                && matchesModelFilter(garage, model)
                && matchesServiceFilter(garage, availableServiceIds);
    }

    /**
     * Checks if garage location matches the city filter.
     * Compares against the state field (case-insensitive).
     */
    private boolean matchesCityFilter(Garage garage, String city) {
        if (StringUtils.isEmpty(city)) {
            return true; // No filter applied
        }
        Location location = garage.getGarageLocation();
        return location != null && city.equalsIgnoreCase(location.getState());
    }

    /**
     * Checks if garage location matches the zip/postal code filter (case-insensitive).
     */
    private boolean matchesZipFilter(Garage garage, String zip) {
        if (StringUtils.isEmpty(zip)) {
            return true; // No filter applied
        }
        Location location = garage.getGarageLocation();
        return location != null && zip.equalsIgnoreCase(location.getPostalCode());
    }

    /**
     * Checks if garage name contains the search term (case-insensitive).
     */
    private boolean matchesNameFilter(Garage garage, String garageName) {
        if (StringUtils.isEmpty(garageName)) {
            return true; // No filter applied
        }
        return garage.getName() != null
                && garage.getName().toLowerCase().contains(garageName.toLowerCase());
    }

    /**
     * Checks if garage serves the specified brand (case-insensitive).
     */
    private boolean matchesBrandFilter(Garage garage, String brand) {
        if (StringUtils.isEmpty(brand)) {
            return true; // No filter applied
        }
        return garage.getBrands() != null
                && garage.getBrands().toLowerCase().contains(brand.toLowerCase());
    }

    /**
     * Checks if garage services the specified model (case-insensitive).
     */
    private boolean matchesModelFilter(Garage garage, String model) {
        if (StringUtils.isEmpty(model)) {
            return true; // No filter applied
        }
        return garage.getModels() != null
                && garage.getModels().toLowerCase().contains(model.toLowerCase());
    }

    /**
     * Checks if garage offers any of the specified services.
     */
    private boolean matchesServiceFilter(Garage garage, List<Integer> availableServiceIds) {
        if (availableServiceIds == null || availableServiceIds.isEmpty()) {
            return true; // No filter applied
        }

        List<Integer> garageServiceIds = garage.getServices().stream()
                .map(GarageService::getAvailableService)
                .map(AvailableService::getId)
                .toList();

        return availableServiceIds.stream().anyMatch(garageServiceIds::contains);
    }

    /**
     * Universal search method that searches across multiple fields in priority order.
     * Returns results from the first field that produces matches (cascade search).
     *
     * @param query      Generic search term to match across multiple fields
     * @param serviceIds Optional service IDs filter
     * @return List of matching garages
     */
    @Transactional(readOnly = true)
    public List<GarageResponse> universalSearch(String query, List<Integer> serviceIds) {
        Set<Garage> allGarages = garageRepository.findAllWithLocationAndServices();

        log.info("Universal search for query: '{}' across {} garages", query, allGarages.size());

        // Try cascade search in priority order - stop at first non-empty result
        List<Garage> results = searchByName(allGarages, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in garage names", results.size());
            return filterAndMapResults(results, serviceIds);
        }

        results = searchByBrand(allGarages, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in brands", results.size());
            return filterAndMapResults(results, serviceIds);
        }

        results = searchByModel(allGarages, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in models", results.size());
            return filterAndMapResults(results, serviceIds);
        }

        results = searchByServiceName(allGarages, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in service names", results.size());
            return filterAndMapResults(results, serviceIds);
        }

        results = searchByCity(allGarages, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in cities", results.size());
            return filterAndMapResults(results, serviceIds);
        }

        results = searchByZip(allGarages, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in zip codes", results.size());
            return filterAndMapResults(results, serviceIds);
        }

        log.info("No matches found for query: '{}'", query);
        return Collections.emptyList();
    }

    /**
     * Search garages by name.
     */
    private List<Garage> searchByName(Set<Garage> garages, String query) {
        return garages.stream()
                .filter(g -> g.getName() != null &&
                        g.getName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Search garages by vehicle brand.
     */
    private List<Garage> searchByBrand(Set<Garage> garages, String query) {
        return garages.stream()
                .filter(g -> g.getBrands() != null &&
                        g.getBrands().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Search garages by vehicle model.
     */
    private List<Garage> searchByModel(Set<Garage> garages, String query) {
        return garages.stream()
                .filter(g -> g.getModels() != null &&
                        g.getModels().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Search garages by service name.
     */
    private List<Garage> searchByServiceName(Set<Garage> garages, String query) {
        return garages.stream()
                .filter(g -> hasServiceMatchingQuery(g, query))
                .toList();
    }

    /**
     * Check if garage has any service matching the query.
     */
    private boolean hasServiceMatchingQuery(Garage garage, String query) {
        return garage.getServices().stream()
                .map(GarageService::getAvailableService)
                .filter(Objects::nonNull)
                .anyMatch(service -> service.getName() != null &&
                        service.getName().toLowerCase().contains(query.toLowerCase()));
    }

    /**
     * Search garages by city or state.
     */
    private List<Garage> searchByCity(Set<Garage> garages, String query) {
        return garages.stream()
                .filter(g -> {
                    Location loc = g.getGarageLocation();
                    if (loc == null) return false;

                    // Check both city and state fields
                    boolean cityMatch = loc.getCity() != null &&
                            loc.getCity().toLowerCase().contains(query.toLowerCase());
                    boolean stateMatch = loc.getState() != null &&
                            loc.getState().toLowerCase().contains(query.toLowerCase());

                    return cityMatch || stateMatch;
                })
                .toList();
    }

    /**
     * Search garages by postal code.
     */
    private List<Garage> searchByZip(Set<Garage> garages, String query) {
        return garages.stream()
                .filter(g -> {
                    Location loc = g.getGarageLocation();
                    return loc != null &&
                            loc.getPostalCode() != null &&
                            loc.getPostalCode().toLowerCase().contains(query.toLowerCase());
                })
                .toList();
    }

    /**
     * Apply service filter if provided, then map to response DTOs.
     */
    private List<GarageResponse> filterAndMapResults(List<Garage> garages, List<Integer> serviceIds) {
        Stream<Garage> stream = garages.stream();

        // Apply service filter if provided
        if (serviceIds != null && !serviceIds.isEmpty()) {
            stream = stream.filter(g -> matchesServiceFilter(g, serviceIds));
        }

        return stream.map(garageMapper::entityToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<Garage> searchGarages2(String brand, String city, String zip, String garageName, List<Integer> availableServiceIds) {


//         Handle null service IDs
        availableServiceIds = availableServiceIds != null ? availableServiceIds : Collections.emptyList();

        // Convert search terms to lowercase once
        final String brandTerm = brand != null ? brand.toLowerCase() : "";
        final String cityTerm = city != null ? city.toLowerCase() : "";
        final String zipTerm = zip != null ? zip.toLowerCase() : "";
        final String garageNameTerm = garageName != null ? garageName.toLowerCase() : "";
        final List<Integer> serviceIds = new ArrayList<>(availableServiceIds);

        // Fetch all necessary data in a single query with joins
        Set<Garage> garages = garageRepository.findAllWithLocationAndServices();


        return garages.stream()
                .filter(Objects::nonNull)
                .filter(garage -> matchesBrand(garage, brandTerm))
                .filter(garage -> matchesCity(garage, cityTerm))
                .filter(garage -> matchesZip(garage, zipTerm))
                .filter(garage -> matchesName(garage, garageNameTerm))
                .filter(garage -> matchesServices(garage, serviceIds))
                .toList();
    }

    private boolean matchesBrand(Garage garage, String brandTerm) {
        return brandTerm.isEmpty() ||
                (garage.getGarageBrandName() != null &&
                        garage.getGarageBrandName().toLowerCase().contains(brandTerm));
    }

    private boolean matchesCity(Garage garage, String cityTerm) {
        return cityTerm.isEmpty() ||
                (garage.getGarageLocation() != null &&
                        garage.getGarageLocation().getCity() != null &&
                        garage.getGarageLocation().getCity().toLowerCase().contains(cityTerm));
    }

    private boolean matchesZip(Garage garage, String zipTerm) {
        return zipTerm.isEmpty() ||
                (garage.getGarageLocation() != null &&
                        garage.getGarageLocation().getArea() != null &&
                        garage.getGarageLocation().getArea().toLowerCase().contains(zipTerm));
    }

    private boolean matchesName(Garage garage, String nameTerm) {
        return nameTerm.isEmpty() ||
                (garage.getName() != null &&
                        garage.getName().toLowerCase().contains(nameTerm));
    }

    private boolean matchesServices(Garage garage, List<Integer> serviceIds) {
        if (serviceIds.isEmpty()) {
            return true;
        }
        if (garage.getServices() == null || garage.getServices().isEmpty()) {
            return false;
        }
        return garage.getServices().stream()
                .map(GarageService::getAvailableService)
                .map(AvailableService::getId)
                .anyMatch(serviceIds::contains);
    }

    public void updateGarageVerification(Integer id) {
        Garage garage = garageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(GARAGE_NOT_FOUND));
        garage.setActive(true);
        garageRepository.save(garage);
    }

    // ====================================================================================
    // FUTURE METHODS: Admin Garage Approval Workflow
    // These methods will be used when admin endpoints are implemented
    // ====================================================================================

    /**
     * FUTURE: Approve a garage (to be called from admin endpoint)
     *
     * @param garageId    The garage ID to approve
     * @param adminUserId The admin user performing the approval
     * @return Updated garage response
     */
    @Transactional
    public GarageResponse approveGarage(Integer garageId, Integer adminUserId) {
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException(GARAGE_NOT_FOUND));

        if (garage.getAccountStatus() != GarageVerificationStatus.PENDING) {
            throw new IllegalStateException("Can only approve PENDING garages");
        }

        LocalDateTime now = LocalDateTime.now();
        garage.setAccountStatus(GarageVerificationStatus.APPROVED);
        garage.setApprovedBy(adminUserId);
        garage.setApprovedAt(now);
        garage.setLastReviewedBy(adminUserId);
        garage.setLastReviewedAt(now);

        // Clear rejection metadata
        garage.setRejectedBy(null);
        garage.setRejectedAt(null);
        garage.setRejectionReason(null);

        Garage saved = garageRepository.save(garage);
        log.info("Garage {} approved by admin {}", garageId, adminUserId);

        return garageMapper.entityToResponse(saved);
    }

    /**
     * FUTURE: Reject a garage (to be called from admin endpoint)
     *
     * @param garageId        The garage ID to reject
     * @param adminUserId     The admin user performing the rejection
     * @param rejectionReason The reason for rejection (required)
     * @return Updated garage response
     */
    @Transactional
    public GarageResponse rejectGarage(Integer garageId, Integer adminUserId, String rejectionReason) {
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        if (rejectionReason.length() > 1000) {
            throw new IllegalArgumentException("Rejection reason cannot exceed 1000 characters");
        }

        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException(GARAGE_NOT_FOUND));

        if (garage.getAccountStatus() != GarageVerificationStatus.PENDING) {
            throw new IllegalStateException("Can only reject PENDING garages");
        }

        LocalDateTime now = LocalDateTime.now();
        garage.setAccountStatus(GarageVerificationStatus.REJECTED);
        garage.setRejectedBy(adminUserId);
        garage.setRejectedAt(now);
        garage.setRejectionReason(rejectionReason.trim());
        garage.setLastReviewedBy(adminUserId);
        garage.setLastReviewedAt(now);

        // Clear approval metadata
        garage.setApprovedBy(null);
        garage.setApprovedAt(null);

        Garage saved = garageRepository.save(garage);
        log.info("Garage {} rejected by admin {}", garageId, adminUserId);

        return garageMapper.entityToResponse(saved);
    }

    /**
     * FUTURE: Resubmit a rejected garage (called by garage owner after fixing issues)
     *
     * @param garageId The garage ID to resubmit
     * @return Updated garage response
     */
    @Transactional
    public GarageResponse resubmitGarage(Integer garageId) {
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException(GARAGE_NOT_FOUND));

        return performResubmission(garage);
    }

    /**
     * Resubmit a rejected garage (owner-facing with authorization)
     *
     * @param garageId The garage ID to resubmit
     * @param ownerId  The owner's user ID from authentication
     * @return Updated garage response
     * @throws EntityNotFoundException     if garage not found
     * @throws UnauthorizedAccessException if user is not the garage owner
     * @throws IllegalStateException       if garage is not in REJECTED status
     */
    @Transactional
    public GarageResponse resubmitGarageByOwner(Integer garageId, Integer ownerId) {
        log.info("Processing resubmission request for garage {} by owner {}", garageId, ownerId);

        // Fetch garage and validate existence
        Garage garage = garageRepository.findById(garageId)
                .orElseThrow(() -> new EntityNotFoundException(GARAGE_NOT_FOUND_WITH_ID + garageId));

        // Authorization: Verify the requester is the garage owner
        if (!garage.getGarageOwner().equals(ownerId)) {
            log.warn("Unauthorized resubmission attempt: User {} tried to resubmit garage {} owned by {}",
                    ownerId, garageId, garage.getGarageOwner());
            throw new UnauthorizedAccessException(
                    "You are not authorized to resubmit this garage. Only the garage owner can resubmit.");
        }

        // Execute resubmission logic
        return performResubmission(garage);
    }

    /**
     * Private helper method to perform the actual resubmission logic.
     * This method contains the shared business logic for resubmitting a rejected garage.
     * It should only be called from within @Transactional methods.
     *
     * @param garage The garage entity to resubmit
     * @return Updated garage response
     * @throws IllegalStateException if garage is not in REJECTED status
     */
    private GarageResponse performResubmission(Garage garage) {
        if (garage.getAccountStatus() != GarageVerificationStatus.REJECTED) {
            throw new IllegalStateException("Can only resubmit REJECTED garages");
        }

        garage.setResubmissionCount(garage.getResubmissionCount() + 1);
        garage.setAccountStatus(GarageVerificationStatus.PENDING);
        garage.setRejectionReason(null);

        Garage saved = garageRepository.save(garage);
        log.info("Garage {} resubmitted (attempt #{})", garage.getId(), garage.getResubmissionCount());

        return garageMapper.entityToResponse(saved);
    }

    // ====================================================================================
    // ADMIN QUERY METHODS: Support for admin controller endpoints
    // ====================================================================================

    /**
     * Get all pending garages
     */
    public List<GarageResponse> getPendingGarages() {
        List<Garage> garages = garageRepository.findAllPendingGarages();
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get all rejected garages
     */
    public List<GarageResponse> getRejectedGarages() {
        List<Garage> garages = garageRepository.findAllRejectedGarages();
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get all approved garages
     */
    public List<GarageResponse> getApprovedGarages() {
        List<Garage> garages = garageRepository.findByAccountStatus(GarageVerificationStatus.APPROVED);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get garages by verification status
     */
    public List<GarageResponse> getGaragesByVerificationStatus(GarageVerificationStatus status) {
        List<Garage> garages = garageRepository.findByAccountStatus(status);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get total garages count
     */
    public long getTotalGaragesCount() {
        return garageRepository.count();
    }

    /**
     * Get garage count by status
     */
    public long getGarageCountByStatus(GarageVerificationStatus status) {
        return garageRepository.countByAccountStatus(status);
    }

    /**
     * Get pending garages older than a specific date
     */
    public List<GarageResponse> getPendingGaragesOlderThan(LocalDateTime cutoffDate) {
        List<Garage> garages = garageRepository.findPendingGaragesOlderThan(cutoffDate);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get garages with multiple resubmissions
     */
    public List<GarageResponse> getGaragesWithMultipleResubmissions(int minCount) {
        List<Garage> garages = garageRepository.findGaragesWithMultipleResubmissions(minCount);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get garages reviewed by specific admin
     */
    public List<GarageResponse> getGaragesReviewedBy(Integer adminId) {
        List<Garage> garages = garageRepository.findByLastReviewedBy(adminId);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get garages approved by specific admin
     */
    public List<GarageResponse> getGaragesApprovedBy(Integer adminId) {
        List<Garage> garages = garageRepository.findByApprovedBy(adminId);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    /**
     * Get garages rejected by specific admin
     */
    public List<GarageResponse> getGaragesRejectedBy(Integer adminId) {
        List<Garage> garages = garageRepository.findByRejectedBy(adminId);
        return garages.stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }
}