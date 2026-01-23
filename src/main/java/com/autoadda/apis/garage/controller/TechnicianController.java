package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.request.TechnicianSearchCriteria;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.response.PagedResponse;
import com.autoadda.apis.garage.response.TechnicianResponse;
import com.autoadda.apis.garage.response.TechnicianWorkHistoryResponse;
import com.autoadda.apis.garage.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/technicians")
@RequiredArgsConstructor
public class TechnicianController {
    private final TechnicianService technicianService;

    @PostMapping
    public ResponseEntity<TechnicianResponse> addTechnician(@RequestBody TechnicianRequest request) {
        TechnicianResponse created = technicianService.onboardTechnician(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<List<TechnicianResponse>> getByGarage(@PathVariable Integer garageId) {
        return ResponseEntity.ok(technicianService.getTechniciansByGarage(garageId));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<TechnicianResponse> getByEmail(@PathVariable String email) {
        return technicianService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<TechnicianResponse>> getBySkill(@PathVariable Integer skillId) {
        List<TechnicianResponse> technicians = technicianService.findBySkillSet(skillId);
        if (technicians.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TechnicianResponse>> getByStatus(@PathVariable String status) {
        List<TechnicianResponse> technicians = technicianService.findByStatus(status);
        if (technicians.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(technicians);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponse> getById(@PathVariable Integer id) {
        return technicianService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Integer id) {
        technicianService.deleteTechnician(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<TechnicianResponse> updateTechnician(@PathVariable Integer id, @RequestBody TechnicianRequest request) {
        return new ResponseEntity<>(technicianService.updateTechnician(id, request), HttpStatus.OK);

    }

    @GetMapping("/autoadda-id/{autoAddaId}")
    public ResponseEntity<TechnicianResponse> findByAutoAddaId(@PathVariable Integer autoAddaId) {
        return technicianService.findByAutoAddaId(autoAddaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Universal and specific search endpoint for technicians (non-paginated)
     * GET /technicians/search
     *
     * Examples:
     * - Universal: /technicians/search?query=John
     * - Specific: /technicians/search?name=John&city=Dubai&skillIds=1,2
     * - Expertise: /technicians/search?expertiseCategories=ENGINE_SYSTEMS,TRANSMISSION&expertiseLevels=L4,L3
     * - Combined: /technicians/search?city=Dubai&expertiseCategories=ENGINE_SYSTEMS&expertiseLevels=L4&skillIds=1
     */
    @GetMapping("/search")
    public ResponseEntity<List<TechnicianResponse>> searchTechnicians(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Integer> skillIds,
            @RequestParam(required = false) List<String> expertiseCategories,
            @RequestParam(required = false) List<String> expertiseLevels,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model) {

        // If query parameter provided, use universal search
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(query)) {
            return ResponseEntity.ok(technicianService.universalSearchViaHelper(
                query, skillIds, expertiseCategories, expertiseLevels));
        }

        // Otherwise use existing specific parameter search
        return ResponseEntity.ok(technicianService.searchTechnicians(
            name, skillIds, expertiseCategories, expertiseLevels, city, brand, model));
    }

    /**
     * Paginated technician search endpoint
     * GET /technicians/search/paginated
     * Supports both universal search (query param) and specific filters
     *
     * Examples:
     * - Universal: /technicians/search/paginated?query=mechanic&page=0&size=10
     * - Specific: /technicians/search/paginated?city=Dubai&skillIds=1,2&sortBy=yearOfExperience&sortDirection=desc
     * - Expertise: /technicians/search/paginated?expertiseCategories=ENGINE_SYSTEMS,TRANSMISSION&expertiseLevels=L4,L3&page=0&size=10
     * - Combined: /technicians/search/paginated?city=Dubai&expertiseCategories=ENGINE_SYSTEMS&expertiseLevels=L4&sortBy=yearOfExperience&sortDirection=desc
     */
    @GetMapping("/search/paginated")
    public ResponseEntity<PagedResponse<TechnicianResponse>> searchTechniciansPaginated(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Integer> skillIds,
            @RequestParam(required = false) List<String> expertiseCategories,
            @RequestParam(required = false) List<String> expertiseLevels,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        TechnicianSearchCriteria criteria = TechnicianSearchCriteria.builder()
                .query(query)
                .name(name)
                .skillIds(skillIds)
                .expertiseCategories(expertiseCategories)
                .expertiseLevels(expertiseLevels)
                .city(city)
                .brand(brand)
                .model(model)
                .build();

        PagedResponse<TechnicianResponse> response = technicianService.searchTechniciansPaginated(
                criteria, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    /**
     * Get technicians by availability status
     * GET /technicians/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<TechnicianResponse>> getTechniciansByAvailability(
            @RequestParam(defaultValue = "true") boolean isAvailable) {

        List<TechnicianResponse> technicians = technicianService.findByAvailability(isAvailable);
        return ResponseEntity.ok(technicians);
    }
    @GetMapping("/garagesByTechnicianId/{id}")
    public ResponseEntity<List<GarageResponse>> getGaragesByTechnicianId(@PathVariable Integer id){
        return ResponseEntity.ok(technicianService.getGaragesByTechnicianId(id));
    }
    @GetMapping("/jobCardHistoryByTechnicianId/{id}")
    public ResponseEntity<List<TechnicianWorkHistoryResponse>> getJobCardHistoryByTechnicianId(@PathVariable Integer id){
        return ResponseEntity.ok(technicianService.getJobCardHistoryByTechnicianId(id));
    }

}
