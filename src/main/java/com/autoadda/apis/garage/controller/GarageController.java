package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.request.GarageRequest;
import com.autoadda.apis.garage.service.GarageManagementService;
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
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/v1/garages")
@RequiredArgsConstructor
public class GarageController {

    private final GarageManagementService garageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGarage(
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestPart("garage") @Valid GarageRequest garage,
            @RequestPart(value = "garageLogo",required = true) MultipartFile garageLogo,
            @RequestPart(value = "vatCertificate", required = false)
            @Size(max = 5, message = "Maximum 5 VAT certificate files allowed")
            List<MultipartFile> vatCertificate,
            @RequestPart(value = "tradeLicenseAttachment", required = false)
            @Size(max = 5, message = "Maximum 5 trade license files allowed")
            List<MultipartFile> tradeLicenseAttachment,
            @RequestPart(value = "garageImages", required = false)
            @Size(max = 10, message = "Maximum 10 garage images allowed")
            List<MultipartFile> garageImages

    ) throws IOException {
        //fall back params
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( garageService.createGarage ( garage, userId, userRole, autoAddaId, garageLogo, vatCertificate, tradeLicenseAttachment, garageImages ) );
    }

    @GetMapping("all")
    public ResponseEntity<List<Garage>> getAllGarages() {
        return ResponseEntity.ok ( garageService.getAllGarages ( ) );
    }

    @GetMapping
    public ResponseEntity<List<Garage>> getAllGaragesByOwnerId(@RequestHeader(value = "X-User-Id") String userId,
                                                               @RequestHeader(value = "X-User-Role") String userRole,
                                                               @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok ( garageService.getAllGaragesByOwnerId ( autoAddaId ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Garage> getGarageById(@PathVariable Integer id) {
        return garageService.getGarageById ( id ).map ( ResponseEntity::ok ).orElse ( ResponseEntity.notFound ( ).build ( ) );
    }

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Garage> updateGarage(@PathVariable Integer id,
                                               @RequestHeader(value = "X-User-Id") String userId,
                                               @RequestHeader(value = "X-User-Role") String userRole,
                                               @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                               @RequestPart(value = "garage", required = false) @Valid GarageRequest garage,
                                               @RequestPart(value = "garageLogo", required = false) @NotNull MultipartFile garageLogo,
                                               @RequestPart(value = "vatCertificate", required = false)
                                               @Size(max = 5, message = "Maximum 5 VAT certificate files allowed")
                                               List<MultipartFile> vatCertificate,
                                               @RequestPart(value = "tradeLicenseAttachment", required = false)
                                               @Size(max = 5, message = "Maximum 5 trade license files allowed")
                                               List<MultipartFile> tradeLicenseAttachment,
                                               @RequestPart(value = "garageImages", required = false)
                                               @Size(max = 10, message = "Maximum 10 garage images allowed")
                                               List<MultipartFile> garageImages) throws IOException {
        HashMap<String, Object> requestData = new HashMap<> ( );
        if ( garage != null )
            requestData.put ( "data", garage );
        if ( tradeLicenseAttachment != null && ! tradeLicenseAttachment.isEmpty ( ) )
            requestData.put ( "tradeLicenseAttachment", tradeLicenseAttachment );
        if ( vatCertificate != null && ! vatCertificate.isEmpty ( ) )
            requestData.put ( "vatCertificate", vatCertificate );
        if ( garageImages != null && ! garageImages.isEmpty ( ) )
            requestData.put ( "garageImages", garageImages );
        if ( garageLogo != null  )
            requestData.put ( "garageLogo", garageLogo );
        return ResponseEntity.ok ( garageService.updateGarage ( id, requestData ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGarage(@PathVariable Integer id) {
        garageService.deleteGarage ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping("/brandname/{name}")
    public ResponseEntity<List<Garage>> searchByLegalName(@RequestParam String name) {
        return ResponseEntity.ok ( garageService.findGaragesByLegalName ( name ) );
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<Garage>> getGaragesByService(@PathVariable Integer serviceId) {
        return ResponseEntity.ok ( garageService.findGaragesOfferingAvailableService ( serviceId ) );
    }
}
