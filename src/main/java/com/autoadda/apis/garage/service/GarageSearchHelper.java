package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.entity.Location;
import com.autoadda.apis.garage.mapper.GarageMapper;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.response.GarageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Helper service for garage search operations.
 * Separated to resolve SonarQube issues with self-invocation of @Transactional methods.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GarageSearchHelper {

    private final GarageRepository garageRepository;
    private final GarageMapper garageMapper;

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

    /**
     * Checks if garage offers any of the specified services.
     */
    private boolean matchesServiceFilter(Garage garage, List<Integer> availableServiceIds) {
        if (availableServiceIds == null || availableServiceIds.isEmpty()) {
            return true; // No filter applied
        }

        List<Integer> garageServiceIds = garage.getServices().stream()
                .filter(GarageService::getIsActive)
                .map(GarageService::getAvailableService)
                .filter(Objects::nonNull)
                .map(AvailableService::getId)
                .toList();

        return availableServiceIds.stream().anyMatch(garageServiceIds::contains);
    }
}
