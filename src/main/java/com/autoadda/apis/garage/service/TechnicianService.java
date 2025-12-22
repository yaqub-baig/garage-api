package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.GarageMapper;
import com.autoadda.apis.garage.mapper.JobCardMapper;
import com.autoadda.apis.garage.mapper.TechnicianMapper;
import com.autoadda.apis.garage.repository.AvailableServiceRepository;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.EMPLOYEE_ROLE;
import com.autoadda.apis.garage.util.ERole;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {
    public static final String TECHNICIAN_NOT_FOUND_WITH_ID = "Technician not found with id: ";
    private final TechnicianMapper technicianMapper;
    private final TechnicianRepository technicianRepository;
    private final GarageRepository garageRepo;
    private final AvailableServiceRepository availableServiceRepository;
    private final AuthClient authClient;
    private final TechnicianSearchHelper technicianSearchHelper;
    private final GarageRepository garageRepository;
    private final GarageMapper garageMapper;
    private final JobCardRepository jobCardRepository;
    private final JobCardMapper jobCardMapper;

    /**
     * Retrieves a list of technicians associated with a specific garage.
     *
     * @param garageId the ID of the garage
     * @return a list of technicians for the specified garage
     */
    @Transactional
    public List<TechnicianResponse> getTechniciansByGarage(Integer garageId) {
        return technicianRepository.findByGaragesContaining(garageId).stream().filter(t -> !t.getIsDeleted()).map(technicianMapper::entityToResponse).toList();
    }

    Technician mapToEntity(TechnicianRequest technicianRequest) {

        return Technician
                .builder()
                .emailId(technicianRequest.getEmailId())
                .vatNumber(technicianRequest.getVatNumber())
                .firstName(technicianRequest.getFirstName())
                .emiratesId(technicianRequest.getEmiratesId())
                .mobileNumber(technicianRequest.getMobileNumber())
                .status(technicianRequest.getStatus() != null ? technicianRequest.getStatus() : Status.ACTIVE)
                .employeeRole(technicianRequest.getEmployeeRole() != null ? technicianRequest.getEmployeeRole() : EMPLOYEE_ROLE.TECHNICIAN)
                .build();
    }

    /**
     * Onboards a new technician to a garage.
     *
     * @param technician the technician request containing details for onboarding
     * @return the saved Technician entity
     */
    @Transactional
    public TechnicianResponse onboardTechnician(TechnicianRequest technician) {

        ResponseEntity<UserResponse> userDetails = authClient.getUserDetails(technician.getMobileNumber());
        if (userDetails == null || !userDetails.getStatusCode().is2xxSuccessful() || userDetails.getBody() == null) {
            log.error("User not found in auth service with phone number: {}", technician.getMobileNumber());
            throw new ResourceNotFoundException("User not found in auth service with phone number: " + technician.getMobileNumber());
        }
        // Build garage set from garageIds or garageId (backward compatibility)
        Set<Garage> garages = new HashSet<>();
        if (technician.getGarageIds() != null && !technician.getGarageIds().isEmpty()) {
            List<Garage> garageList = garageRepo.findAllById(technician.getGarageIds());
            if (garageList.size() != technician.getGarageIds().size()) {
                throw new ResourceNotFoundException("One or more garage IDs not found");
            }
            garages.addAll(garageList);
        } else if (technician.getGarageId() != null) {
            // Backward compatibility: support single garageId
            Garage garage = garageRepo.findById(technician.getGarageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Garage not found"));
            garages.add(garage);
        }

        Technician garageTechnician = mapToEntity(technician);
        Optional<Technician> existingTechnician = technicianRepository.findByMobileNumber(technician.getMobileNumber());
        if (existingTechnician.isPresent() && !existingTechnician.get().getIsDeleted()) {
            throw new AlreadyExistsException("Technician with this phone number already exists");
        }
        if (existingTechnician.isPresent() && existingTechnician.get().getIsDeleted()) {
            existingTechnician.get().setIsDeleted(false);
            garageTechnician = existingTechnician.get();
        }

        garageTechnician.setFirstName(technician.getFirstName());
        garageTechnician.setEmailId(technician.getEmailId());
        garageTechnician.setGarages(garages);  // Use garages list for many-to-many
        garageTechnician.setSkillSet(availableServiceRepository.findAllById(technician.getSkillSetIds()));

        // Set new fields
        garageTechnician.setBrand(technician.getBrand());
        garageTechnician.setModel(technician.getModel());
        garageTechnician.setDescription(technician.getDescription());
        garageTechnician.setYearOfExperience(technician.getYearOfExperience());
        garageTechnician.setCity(technician.getCity());
        garageTechnician.setIsAvailableForService(
                technician.getIsAvailableForService() != null
                        ? technician.getIsAvailableForService()
                        : true
        );


        garageTechnician.setAutoAddaId(userDetails.getBody().getId());
        authClient.updateRole(garageTechnician.getAutoAddaId(), ERole.GARAGE_TECH);

        return technicianMapper.entityToResponse(technicianRepository.saveAndFlush(garageTechnician));
    }

    /**
     * Finds a technician by their email address.
     *
     * @param email the email address of the technician
     * @return an Optional containing the Technician if found, or empty if not found
     */
    @Transactional
    public Optional<TechnicianResponse> findByEmail(String email) {
        return technicianRepository.findByEmailId(email).map(technicianMapper::entityToResponse);
    }

    /**
     * Deletes a technician by their ID.
     *
     * @param id the ID of the technician to be deleted
     */
    @Transactional
    public void deleteTechnician(Integer id) {
        Technician technician = technicianRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("technician not found"));
        technician.setIsDeleted(true);
        technicianRepository.save(technician);
    }

    /**
     * Finds a technician by their ID.
     *
     * @param id the ID of the technician
     * @return an Optional containing the Technician if found, or empty if not found
     */
    @Transactional
    public Optional<TechnicianResponse> findById(Integer id) {
        return technicianRepository.findById(id)
                .map(technicianMapper::entityToResponse);
    }

    /**
     * Finds technicians by their skill set.
     *
     * @param skillId the skill to search for
     * @return a list of technicians with the specified skill
     */
    @Transactional
    public List<TechnicianResponse> findBySkillSet(Integer skillId) {
        return technicianRepository.findBySkillSetId(skillId).stream().filter(t -> !t.getIsDeleted()).map(technicianMapper::entityToResponse).toList();
    }

    /**
     * Finds technicians by their status.
     *
     * @param status the status to search for
     * @return a list of technicians with the specified status
     */
    @Transactional
    public List<TechnicianResponse> findByStatus(String status) {
        return technicianRepository
                .findByStatus(status)
                .stream()
                .filter(t -> !t.getIsDeleted())
                .map(technicianMapper::entityToResponse).toList();
    }

    /**
     * Finds a technician by their phone number.
     *
     * @param phone the phone number of the technician
     * @return an Optional containing the Technician if found, or empty if not found
     */
    @Transactional
    public Optional<Technician> findByPhone(String phone) {
        return technicianRepository.findByMobileNumber(phone);
    }

    /**
     * Updates an existing technician's details.
     *
     * @param id
     * @param technicianRequest the request containing updated technician details
     * @return the updated Technician entity
     */
    @Transactional
    public TechnicianResponse updateTechnician(Integer id, TechnicianRequest technicianRequest) {
        Technician technician = technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND_WITH_ID + id));
        if (technicianRequest.getGarageIds() != null) {
            technicianRequest.getGarageIds().forEach(garageId -> {
                Garage garage = garageRepository.findById(garageId)
                        .orElseThrow(() -> new ResourceNotFoundException("Garage not found with id: " + garageId));
                technician.getGarages().add(garage);
            });
        }
        Technician garageTechnician = technicianMapper.updateTechnician(technician, technicianRequest);
        if (technicianRequest.getSkillSetIds() != null && !technicianRequest.getSkillSetIds().isEmpty()) {
            log.info("Updating 1 skill set for technician id {}: {}", id, garageTechnician.getSkillSet().stream().map(AvailableService::getId).toList());
            garageTechnician.getSkillSet().clear();
            log.info("Updating 2 skill set for technician id {}: {}", id, garageTechnician.getSkillSet().stream().map(AvailableService::getId).toList());
            garageTechnician.getSkillSet().addAll(availableServiceRepository.findAllById(technicianRequest.getSkillSetIds()));
            log.info("Updating 3 skill set for technician id {}: {}", id, garageTechnician.getSkillSet().stream().map(AvailableService::getId).toList());
        }
        return technicianMapper.entityToResponse(technicianRepository.save(garageTechnician));
    }

    /**
     * Retrieves all technicians.
     *
     * @return a list of all technicians
     */
    @Transactional
    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }

    /**
     * Retrieves a technician by their ID.
     *
     * @param id the ID of the technician
     * @return the Technician entity if found
     * @throws RuntimeException if the technician is not found
     */
    @Transactional
    public Technician getTechnicianById(Integer id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(TECHNICIAN_NOT_FOUND_WITH_ID + id));
    }

    public Optional<TechnicianResponse> findByAutoAddaId(Integer autoAddaId) {
        return technicianRepository.findByAutoAddaId(autoAddaId)
                .map(technicianMapper::entityToResponse);
    }

    /**
     * Universal search wrapper - delegates to TechnicianSearchHelper
     * Resolves SonarQube warning about calling @Transactional methods via 'this'.
     */
    public List<TechnicianResponse> universalSearchViaHelper(String query, List<Integer> skillIds) {
        return technicianSearchHelper.universalSearch(query, skillIds);
    }

    /**
     * Specific filter search for technicians (non-paginated)
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> searchTechnicians(
            String name, List<Integer> skillIds, String city, String brand, String model) {

        Set<Technician> technicians = technicianRepository.findAllActiveWithEagerLoading();
        log.info("Searching through {} technicians with filters - name: {}, city: {}, brand: {}, model: {}, skillIds: {}",
                technicians.size(), name, city, brand, model, skillIds);

        List<Technician> searchResults = technicians.stream()
                .filter(t -> matchesSearchCriteria(t, name, skillIds, city, brand, model))
                .toList();

        log.info("Search found {} matching technicians", searchResults.size());
        return searchResults.stream().map(technicianMapper::entityToResponse).toList();
    }

    /**
     * Paginated search for technicians with specific filters or universal query
     */
    @Transactional(readOnly = true)
    public PagedResponse<TechnicianResponse> searchTechniciansPaginated(
            String query, String name, List<Integer> skillIds, String city, String brand, String model,
            int page, int size, String sortBy, String sortDirection) {

        log.info("Paginated search - query: '{}', page: {}, size: {}, sortBy: {}, sortDir: {}",
                query, page, size, sortBy, sortDirection);

        if (StringUtils.isNotEmpty(query)) {
            List<TechnicianResponse> universalResults = technicianSearchHelper.universalSearch(query, skillIds);
            return paginateTechnicianResponses(universalResults, page, size, sortBy, sortDirection);
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (hasNoFilters(name, skillIds, city, brand, model)) {
            Page<Technician> technicianPage = technicianRepository.findAllActiveWithPagination(pageable);
            return convertToPagedResponse(technicianPage);
        }

        Set<Technician> allTechnicians = technicianRepository.findAllActiveWithEagerLoading();
        List<Technician> filtered = allTechnicians.stream()
                .filter(t -> matchesSearchCriteria(t, name, skillIds, city, brand, model))
                .sorted(getTechnicianComparator(sortBy, direction))
                .toList();

        log.info("Filtered {} technicians from {} total", filtered.size(), allTechnicians.size());
        return paginateList(filtered, page, size);
    }

    @Transactional(readOnly = true)
    public List<TechnicianResponse> findByAvailability(boolean isAvailable) {
        return technicianRepository.findAll().stream()
                .filter(t -> !t.getIsDeleted())
                .filter(t -> t.getStatus() == Status.ACTIVE)
                .filter(t -> Boolean.TRUE.equals(t.getIsAvailableForService()) == isAvailable)
                .map(technicianMapper::entityToResponse)
                .toList();
    }

    private boolean hasNoFilters(String name, List<Integer> skillIds, String city, String brand, String model) {
        return (name == null || name.isEmpty()) &&
                (skillIds == null || skillIds.isEmpty()) &&
                (city == null || city.isEmpty()) &&
                (brand == null || brand.isEmpty()) &&
                (model == null || model.isEmpty());
    }

    private boolean matchesSearchCriteria(Technician technician, String name, List<Integer> skillIds,
                                          String city, String brand, String model) {
        return matchesNameFilter(technician, name)
                && matchesSkillFilter(technician, skillIds)
                && matchesCityFilter(technician, city)
                && matchesBrandFilter(technician, brand)
                && matchesModelFilter(technician, model);
    }

    private boolean matchesNameFilter(Technician technician, String name) {
        if (StringUtils.isEmpty(name)) {
            return true;
        }
        return technician.getFirstName() != null
                && technician.getFirstName().toLowerCase().contains(name.toLowerCase());
    }

    private boolean matchesSkillFilter(Technician technician, List<Integer> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return true;
        }
        List<Integer> technicianSkillIds = technician.getSkillSet().stream()
                .filter(Objects::nonNull)
                .map(AvailableService::getId)
                .toList();
        return skillIds.stream().anyMatch(technicianSkillIds::contains);
    }

    private boolean matchesCityFilter(Technician technician, String city) {
        if (StringUtils.isEmpty(city)) {
            return true;
        }
        return technician.getCity() != null
                && technician.getCity().equalsIgnoreCase(city);
    }

    private boolean matchesBrandFilter(Technician technician, String brand) {
        if (StringUtils.isEmpty(brand)) {
            return true;
        }
        return technician.getBrand() != null
                && technician.getBrand().toLowerCase().contains(brand.toLowerCase());
    }

    private boolean matchesModelFilter(Technician technician, String model) {
        if (StringUtils.isEmpty(model)) {
            return true;
        }
        return technician.getModel() != null
                && technician.getModel().toLowerCase().contains(model.toLowerCase());
    }

    private Comparator<Technician> getTechnicianComparator(String sortBy, Sort.Direction direction) {
        Comparator<Technician> comparator = switch (sortBy) {
            case "firstName" -> Comparator.comparing(Technician::getFirstName,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "city" -> Comparator.comparing(Technician::getCity,
                    Comparator.nullsLast(String::compareToIgnoreCase));
            case "yearOfExperience" -> Comparator.comparing(Technician::getYearOfExperience,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdAt" -> Comparator.comparing(Technician::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case "id" -> Comparator.comparing(Technician::getId);
            default -> Comparator.comparing(Technician::getId);
        };
        return direction == Sort.Direction.DESC ? comparator.reversed() : comparator;
    }

    private PagedResponse<TechnicianResponse> paginateList(List<Technician> allItems, int page, int size) {
        int totalElements = allItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        List<Technician> pageContent = (start < totalElements)
                ? allItems.subList(start, end)
                : Collections.emptyList();
        List<TechnicianResponse> content = pageContent.stream()
                .map(technicianMapper::entityToResponse)
                .toList();
        return PagedResponse.<TechnicianResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(content.isEmpty())
                .numberOfElements(content.size())
                .build();
    }

    private PagedResponse<TechnicianResponse> convertToPagedResponse(Page<Technician> technicianPage) {
        List<TechnicianResponse> content = technicianPage.getContent().stream()
                .map(technicianMapper::entityToResponse)
                .toList();
        return PagedResponse.<TechnicianResponse>builder()
                .content(content)
                .pageNumber(technicianPage.getNumber())
                .pageSize(technicianPage.getSize())
                .totalElements(technicianPage.getTotalElements())
                .totalPages(technicianPage.getTotalPages())
                .first(technicianPage.isFirst())
                .last(technicianPage.isLast())
                .empty(technicianPage.isEmpty())
                .numberOfElements(technicianPage.getNumberOfElements())
                .build();
    }

    private PagedResponse<TechnicianResponse> paginateTechnicianResponses(
            List<TechnicianResponse> allResults, int page, int size,
            String sortBy, String sortDirection) {
        Comparator<TechnicianResponse> comparator = switch (sortBy) {
            case "firstName" -> Comparator.comparing(
                    tr -> tr.getFirstName() != null ? tr.getFirstName() : "",
                    String.CASE_INSENSITIVE_ORDER);
            case "city" -> Comparator.comparing(
                    tr -> tr.getCity() != null ? tr.getCity() : "",
                    String.CASE_INSENSITIVE_ORDER);
            case "yearOfExperience" -> Comparator.comparing(
                    tr -> tr.getYearOfExperience() != null ? tr.getYearOfExperience() : 0);
            case "id" -> Comparator.comparing(
                    tr -> tr.getId() != null ? tr.getId() : 0);
            default -> Comparator.comparing(
                    tr -> tr.getId() != null ? tr.getId() : 0);
        };
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        List<TechnicianResponse> sortedResults = allResults.stream()
                .sorted(comparator)
                .toList();
        int totalElements = sortedResults.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        List<TechnicianResponse> pageContent = (start < totalElements)
                ? sortedResults.subList(start, end)
                : Collections.emptyList();
        return PagedResponse.<TechnicianResponse>builder()
                .content(pageContent)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .empty(pageContent.isEmpty())
                .numberOfElements(pageContent.size())
                .build();
    }

    public List<GarageResponse> getGaragesByTechnicianId(Integer id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND_WITH_ID + id))
                .getGarages()
                .stream()
                .map(garageMapper::entityToResponse)
                .toList();
    }

    public List<TechnicianWorkHistoryResponse> getJobCardHistoryByTechnicianId(Integer id) {
        return   jobCardRepository.
                 findJobCardHistoryByTechnicianId(id)
                .stream()
                .map(jobCardMapper::entityToTechnicianWorkHistoryResponse)
                .toList();
    }
}
