package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.request.GarageSearchCriteria;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.response.PagedResponse;
import com.autoadda.apis.garage.response.UserResponse;
import com.autoadda.apis.garage.service.GarageManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/garages")
@RequiredArgsConstructor
public class GarageController {

    private final GarageManagementService garageService;


    @PostMapping
    public ResponseEntity<GarageResponse> createGarage(
            HttpServletRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody @Valid GarageRequest garage
    ) {
        //fall back params
        log.info("Content-Length: {}", request.getHeader("Content-Length"));
        log.info("Content-Type: {}", request.getContentType());

        return ResponseEntity.status(HttpStatus.CREATED).body(garageService.createGarage(garage, autoAddaId));
    }

    @GetMapping("all")
    public ResponseEntity<List<GarageResponse>> getAllGarages() {
        return ResponseEntity.ok(garageService.getAllGarages());
    }

    @GetMapping
    public ResponseEntity<List<GarageResponse>> getAllGaragesByOwnerId(@RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok(garageService.getAllGaragesByOwnerId(autoAddaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GarageResponse> getGarageById(@PathVariable Integer id) {
        return garageService.getGarageById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<GarageResponse> updateGarage(@PathVariable Integer id,
                                                       @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                                       @RequestBody GarageRequest garage) {

        return ResponseEntity.ok(garageService.updateGarage(id, garage));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateGarageVerification(@PathVariable Integer id) {
        garageService.updateGarageVerification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGarage(@PathVariable Integer id) {
        garageService.deleteGarage(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resubmit a rejected garage for admin review
     * POST /garages/{id}/resubmit
     */
    @PostMapping("/{id}/resubmit")
    public ResponseEntity<GarageResponse> resubmitGarage(
            @PathVariable Integer id,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {

        log.info("Garage owner {} requesting resubmission for garage {}", autoAddaId, id);
        GarageResponse response = garageService.resubmitGarageByOwner(id, Integer.parseInt(autoAddaId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brandname/{name}")
    public ResponseEntity<List<GarageResponse>> searchByLegalName(@PathVariable String name) {
        return ResponseEntity.ok(garageService.findGaragesByLegalName(name));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<GarageResponse>> getGaragesByService(@PathVariable Integer serviceId) {
        return ResponseEntity.ok(garageService.findGaragesOfferingAvailableService(serviceId));
    }

    //search garage based on  city,postal code,available services ,brand, garage name
    // Universal search using 'query' parameter searches across all fields

    @GetMapping("/search")
    public ResponseEntity<List<GarageResponse>> searchGarages(
            @RequestParam(required = false) String query,              // Universal search parameter
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String garageName,
            @RequestParam(required = false) List<Integer> availableServiceIds) {

        // If query parameter provided, use universal search
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(query)) {
            return ResponseEntity.ok(garageService.universalSearchViaHelper(query, availableServiceIds));
        }

        // Otherwise use existing specific parameter search
        return ResponseEntity.ok(garageService.searchGarages(brand, city, zip, garageName, availableServiceIds, model));
    }

    /**
     * Paginated garage search endpoint
     * GET /garages/search/paginated
     * Supports both universal search (query param) and specific filters
     */
    @GetMapping("/search/paginated")
    public ResponseEntity<PagedResponse<GarageResponse>> searchGaragesPaginated(
            @RequestParam(required = false) String query,              // Universal search parameter
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String garageName,
            @RequestParam(required = false) List<Integer> availableServiceIds,
            @RequestParam(required = false) String model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        GarageSearchCriteria criteria = GarageSearchCriteria.builder()
                .query(query)
                .brand(brand)
                .city(city)
                .zip(zip)
                .garageName(garageName)
                .availableServiceIds(availableServiceIds)
                .model(model)
                .build();

        PagedResponse<GarageResponse> response = garageService.searchGaragesPaginated(
                criteria, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{garageId}/customers")
    public ResponseEntity<List<UserResponse>> getCustomersByGarageId(@PathVariable Integer garageId) {
        List<UserResponse> customers = garageService.getUniqueCustomersByGarageId(garageId);
        return ResponseEntity.ok(customers);
    }


}
