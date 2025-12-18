package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.mapper.TechnicianMapper;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.response.TechnicianResponse;
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
 * Helper service for technician search operations.
 * Separated to resolve SonarQube issues with self-invocation of @Transactional methods.
 *
 * Search Priority (Cascade):
 * 1. Name (firstName)
 * 2. Skills (skillSet)
 * 3. Brand specialization
 * 4. Model specialization
 * 5. City
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TechnicianSearchHelper {

    private final TechnicianRepository technicianRepository;
    private final TechnicianMapper technicianMapper;

    /**
     * Universal search method that searches across multiple fields in priority order.
     * Returns results from the first field that produces matches (cascade search).
     *
     * @param query      Generic search term to match across multiple fields
     * @param skillIds   Optional skill/service IDs filter
     * @return List of matching technicians
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> universalSearch(String query, List<Integer> skillIds) {
        Set<Technician> allTechnicians = technicianRepository.findAllActiveWithEagerLoading();

        log.info("Universal search for query: '{}' across {} technicians", query, allTechnicians.size());

        // Try cascade search in priority order - stop at first non-empty result
        List<Technician> results = searchByName(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in technician names", results.size());
            return filterAndMapResults(results, skillIds);
        }

        results = searchBySkills(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in skills", results.size());
            return filterAndMapResults(results, skillIds);
        }

        results = searchByBrand(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in brand specialization", results.size());
            return filterAndMapResults(results, skillIds);
        }

        results = searchByModel(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in model specialization", results.size());
            return filterAndMapResults(results, skillIds);
        }

        results = searchByCity(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in cities", results.size());
            return filterAndMapResults(results, skillIds);
        }

        log.info("No matches found for query: '{}'", query);
        return Collections.emptyList();
    }

    /**
     * Search technicians by name (firstName).
     */
    private List<Technician> searchByName(Set<Technician> technicians, String query) {
        return technicians.stream()
                .filter(t -> t.getFirstName() != null &&
                        t.getFirstName().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Search technicians by skill set names.
     */
    private List<Technician> searchBySkills(Set<Technician> technicians, String query) {
        return technicians.stream()
                .filter(t -> hasSkillMatchingQuery(t, query))
                .toList();
    }

    /**
     * Check if technician has any skill matching the query.
     */
    private boolean hasSkillMatchingQuery(Technician technician, String query) {
        return technician.getSkillSet().stream()
                .filter(Objects::nonNull)
                .anyMatch(skill -> skill.getName() != null &&
                        skill.getName().toLowerCase().contains(query.toLowerCase()));
    }

    /**
     * Search technicians by brand specialization.
     */
    private List<Technician> searchByBrand(Set<Technician> technicians, String query) {
        return technicians.stream()
                .filter(t -> t.getBrand() != null &&
                        t.getBrand().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Search technicians by model specialization.
     */
    private List<Technician> searchByModel(Set<Technician> technicians, String query) {
        return technicians.stream()
                .filter(t -> t.getModel() != null &&
                        t.getModel().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Search technicians by city.
     */
    private List<Technician> searchByCity(Set<Technician> technicians, String query) {
        return technicians.stream()
                .filter(t -> t.getCity() != null &&
                        t.getCity().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    /**
     * Apply skill filter if provided, then map to response DTOs.
     */
    private List<TechnicianResponse> filterAndMapResults(List<Technician> technicians, List<Integer> skillIds) {
        Stream<Technician> stream = technicians.stream();

        // Apply skill filter if provided
        if (skillIds != null && !skillIds.isEmpty()) {
            stream = stream.filter(t -> matchesSkillFilter(t, skillIds));
        }

        return stream.map(technicianMapper::entityToResponse).toList();
    }

    /**
     * Checks if technician has any of the specified skills.
     */
    private boolean matchesSkillFilter(Technician technician, List<Integer> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return true; // No filter applied
        }

        List<Integer> technicianSkillIds = technician.getSkillSet().stream()
                .filter(Objects::nonNull)
                .map(AvailableService::getId)
                .toList();

        return skillIds.stream().anyMatch(technicianSkillIds::contains);
    }
}
