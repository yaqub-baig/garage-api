package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.service.AppointmentSlotService;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.service.MediaFileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/garages")
@RequiredArgsConstructor
public class GarageController {
    private final AppointmentSlotService bookingService;
    private final GarageManagementService garageService;
    private final MediaFileService mediaFileService;

    @PostMapping
    public ResponseEntity<GarageResponse> createGarage(
            HttpServletRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody @Valid GarageRequest garage
    ) {
        //fall back params
        log.info ( "Content-Length: {}", request.getHeader ( "Content-Length" ) );
        log.info ( "Content-Type: {}", request.getContentType ( ) );

        return ResponseEntity.status ( HttpStatus.CREATED ).body ( garageService.createGarage ( garage, autoAddaId ) );
    }

    @GetMapping("all")
    public ResponseEntity<List<GarageResponse>> getAllGarages() {
        return ResponseEntity.ok ( garageService.getAllGarages ( ) );
    }

    @GetMapping
    public ResponseEntity<List<GarageResponse>> getAllGaragesByOwnerId(@RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok ( garageService.getAllGaragesByOwnerId ( autoAddaId ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GarageResponse> getGarageById(@PathVariable Integer id) {
        return garageService.getGarageById ( id ).map ( ResponseEntity::ok ).orElse ( ResponseEntity.notFound ( ).build ( ) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<GarageResponse> updateGarage(@PathVariable Integer id,
                                                       @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                                       @RequestBody GarageRequest garage) throws IOException {

        return ResponseEntity.ok ( garageService.updateGarage ( id, garage ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGarage(@PathVariable Integer id) {
        garageService.deleteGarage ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping("/brandname/{name}")
    public ResponseEntity<List<GarageResponse>> searchByLegalName(@RequestParam String name) {
        return ResponseEntity.ok ( garageService.findGaragesByLegalName ( name ) );
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<GarageResponse>> getGaragesByService(@PathVariable Integer serviceId) {
        return ResponseEntity.ok ( garageService.findGaragesOfferingAvailableService ( serviceId ) );
    }

    //search garage based on  city,postal code,available services ,brand, garage name

    @GetMapping("/search")
    public ResponseEntity<List<GarageResponse>> searchGarages(@RequestParam(required = false) String model,@RequestParam(required = false) String brand, @RequestParam(required = false) String city, @RequestParam(required = false) String zip, @RequestParam(required = false) String garageName, @RequestParam(required = false) List<Integer> availableServiceIds) {
        return ResponseEntity.ok ( garageService.searchGarages ( brand, city, zip, garageName, availableServiceIds, model ) );
    }


}
