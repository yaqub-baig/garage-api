package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.service.GarageManagementService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v1/garages")
@RequiredArgsConstructor
public class GarageController {

    private final GarageManagementService garageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGarage(
            @RequestHeader(value = "X-User-Id")  String userId,
            @RequestHeader(value = "X-User-Role")  String userRole,
            @RequestHeader(value = "X-Auto-Adda-Id")  String autoAddaId,
            @RequestPart("garage") @Valid GarageRequest garage,
            @RequestPart("garageLogo") @NotNull MultipartFile garageLogo,
            @RequestPart(value = "vatCertificate", required = false)
            @Size(max = 5, message = "Maximum 5 VAT certificate files allowed")
            List< MultipartFile> vatCertificate,
            @RequestPart(value = "tradeLicenseAttachment", required = false)
            @Size(max = 5, message = "Maximum 5 trade license files allowed")
            List< MultipartFile> tradeLicenseAttachment,
            @RequestPart(value = "garageImages", required = false)
            @Size(max = 10, message = "Maximum 10 garage images allowed")
            List< MultipartFile> garageImages

    ) throws IOException {
        //fall back params
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( garageService.createGarage ( garage, userId, userRole, autoAddaId, garageLogo, vatCertificate, tradeLicenseAttachment, garageImages ) );
    }

    @GetMapping
    public ResponseEntity<List<Garage>> getAllGarages() {
        return ResponseEntity.ok ( garageService.getAllGarages ( ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Garage> getGarageById(@PathVariable Integer id) {
        return garageService.getGarageById ( id ).map ( ResponseEntity::ok ).orElse ( ResponseEntity.notFound ( ).build ( ) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Garage> updateGarage(@PathVariable Integer id, @RequestBody GarageRequest garage) {
        return ResponseEntity.ok ( garageService.updateGarage ( id, garage ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGarage(@PathVariable Integer id) {
        garageService.deleteGarage ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping("/legal-name")
    public ResponseEntity<List<Garage>> searchByLegalName(@RequestParam String name) {
        return ResponseEntity.ok ( garageService.findGaragesByLegalName ( name ) );
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<Garage>> getGaragesByService(@PathVariable Integer serviceId) {
        return ResponseEntity.ok ( garageService.findGaragesOfferingAvailableService ( serviceId ) );
    }
}
