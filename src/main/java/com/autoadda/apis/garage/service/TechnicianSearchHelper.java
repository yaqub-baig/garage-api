package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.AvailableService;
import com.autoadda.apis.garage.entity.ExpertiseCategory;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.mapper.TechnicianMapper;
import com.autoadda.apis.garage.repository.ExpertiseCategoryRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.response.TechnicianResponse;
import com.autoadda.apis.garage.util.ExpertiseLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final ExpertiseCategoryRepository expertiseCategoryRepository;

    /**
     * Universal search method that searches across multiple fields in priority order.
     * Returns results from the first field that produces matches (cascade search).
     *
     * @param query               Generic search term to match across multiple fields
     * @param skillIds            Optional skill/service IDs filter
     * @param expertiseCategories Optional expertise category filters
     * @param expertiseLevels     Optional expertise level filters (paired with categories)
     * @return List of matching technicians
     */
    @Transactional(readOnly = true)
    public List<TechnicianResponse> universalSearch(String query, List<Integer> skillIds,
                                                    List<String> expertiseCategories,
                                                    List<String> expertiseLevels) {
        Set<Technician> allTechnicians = technicianRepository.findAllActiveWithEagerLoading();

        log.info("Universal search for query: '{}' across {} technicians", query, allTechnicians.size());

        // Try cascade search in priority order - stop at first non-empty result
        List<Technician> results = searchByName(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in technician names", results.size());
            return filterAndMapResults(results, skillIds, expertiseCategories, expertiseLevels);
        }

        results = searchBySkills(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in skills", results.size());
            return filterAndMapResults(results, skillIds, expertiseCategories, expertiseLevels);
        }

        // NEW: Expertise search
        results = searchByExpertise(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in expertise categories", results.size());
            return filterAndMapResults(results, skillIds, expertiseCategories, expertiseLevels);
        }

        results = searchByBrand(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in brand specialization", results.size());
            return filterAndMapResults(results, skillIds, expertiseCategories, expertiseLevels);
        }

        results = searchByModel(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in model specialization", results.size());
            return filterAndMapResults(results, skillIds, expertiseCategories, expertiseLevels);
        }

        results = searchByCity(allTechnicians, query);
        if (!results.isEmpty()) {
            log.info("Found {} matches in cities", results.size());
            return filterAndMapResults(results, skillIds, expertiseCategories, expertiseLevels);
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
     * Search technicians by expertise category names.
     * Matches against enum name (e.g., "ENGINE" matches ENGINE_SYSTEMS).
     */
    private List<Technician> searchByExpertise(Set<Technician> technicians, String query) {
        return technicians.stream()
                .filter(t -> hasExpertiseMatchingQuery(t, query))
                .toList();
    }

    /**
     * Check if technician has expertise category matching query.
     * Case-insensitive partial match on enum name.
     */
    private boolean hasExpertiseMatchingQuery(Technician technician, String query) {
        if (technician.getExpertise() == null || technician.getExpertise().isEmpty()) {
            return false;
        }

        return technician.getExpertise().stream()
                .filter(Objects::nonNull)
                .anyMatch(exp -> exp.getExpertiseCategory() != null &&
                        exp.getExpertiseCategory().getName() != null &&
                        exp.getExpertiseCategory().getName().toLowerCase()
                           .contains(query.toLowerCase()));
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
     * Apply skill and expertise filters if provided, then map to response DTOs.
     */
    private List<TechnicianResponse> filterAndMapResults(
            List<Technician> technicians, List<Integer> skillIds,
            List<String> expertiseCategories, List<String> expertiseLevels) {

        Stream<Technician> stream = technicians.stream();

        // Apply skill filter if provided
        if (skillIds != null && !skillIds.isEmpty()) {
            stream = stream.filter(t -> matchesSkillFilter(t, skillIds));
        }

        // Apply expertise filter if provided
        List<ExpertisePair> expertisePairs = parseExpertiseFilters(
            expertiseCategories, expertiseLevels);
        if (!expertisePairs.isEmpty()) {
            stream = stream.filter(t -> matchesExpertiseFilter(t, expertisePairs));
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

    /**
     * Represents a category-level pair for expertise filtering.
     */
    private record ExpertisePair(ExpertiseCategory category, ExpertiseLevel level) {}

    /**
     * Validates and parses expertise filter parameters.
     * Position-based pairing: categories[i] pairs with levels[i].
     *
     * @param categories List of expertise category strings
     * @param levels List of expertise level strings
     * @return List of valid category-level pairs, or empty list if invalid
     */
    private List<ExpertisePair> parseExpertiseFilters(
            List<String> categories, List<String> levels) {

        if (categories == null || categories.isEmpty() ||
            levels == null || levels.isEmpty()) {
            return Collections.emptyList();
        }

        if (categories.size() != levels.size()) {
            log.warn("Expertise filter size mismatch: {} categories, {} levels",
                     categories.size(), levels.size());
            return Collections.emptyList();
        }

        List<ExpertisePair> pairs = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            try {
                ExpertiseCategory category = expertiseCategoryRepository
                        .findByNameIgnoreCase(categories.get(i))
                        .orElse(null);
                if (category == null) {
                    log.warn("Invalid expertise filter: category={}, level={}",
                             categories.get(i), levels.get(i));
                    continue;
                }
                ExpertiseLevel level = ExpertiseLevel.valueOf(
                    levels.get(i).toUpperCase());
                pairs.add(new ExpertisePair(category, level));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid expertise filter: category={}, level={}",
                         categories.get(i), levels.get(i));
                // Skip invalid pairs, continue processing
            }
        }

        return pairs;
    }

    /**
     * Checks if technician has ANY of the specified expertise pairs.
     * OR logic - returns true if any pair matches exactly.
     *
     * @param technician The technician to check
     * @param expertisePairs List of category-level pairs to match
     * @return true if technician has at least one matching expertise, false otherwise
     */
    private boolean matchesExpertiseFilter(Technician technician,
                                           List<ExpertisePair> expertisePairs) {
        if (expertisePairs == null || expertisePairs.isEmpty()) {
            return true; // No filter applied
        }

        if (technician.getExpertise() == null || technician.getExpertise().isEmpty()) {
            return false; // No expertise, cannot match
        }

        return expertisePairs.stream().anyMatch(pair ->
            technician.getExpertise().stream()
                .filter(Objects::nonNull)
                .anyMatch(exp ->
                    exp.getExpertiseCategory() != null &&
                    Objects.equals(exp.getExpertiseCategory().getId(), pair.category().getId()) &&
                    exp.getExpertiseLevel() == pair.level()
                )
        );
    }
}
