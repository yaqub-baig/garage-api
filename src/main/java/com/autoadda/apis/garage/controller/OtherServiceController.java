package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.OtherService;
import com.autoadda.apis.garage.request.OtherServiceRequest;
import com.autoadda.apis.garage.service.OtherServiceManagementService;
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
@RequestMapping("/other-services")
@RequiredArgsConstructor
public class OtherServiceController {

    private final OtherServiceManagementService otherService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createOtherService(
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestPart("otherService") OtherServiceRequest otherServiceRequest,
            @RequestPart(value = "companyLogo", required = true) MultipartFile companyLogo,
            @RequestPart(value = "vatCertificate", required = false)
            @Size(max = 5, message = "Maximum 5 VAT certificate files allowed")
            List<MultipartFile> vatCertificate,
            @RequestPart(value = "tradeLicenseAttachment", required = false)
            @Size(max = 5, message = "Maximum 5 trade license files allowed")
            List<MultipartFile> tradeLicenseAttachment,
            @RequestPart(value = "companyImages", required = false)
            @Size(max = 10, message = "Maximum 10 company images allowed")
            List<MultipartFile> companyImages) throws IOException {
        return ResponseEntity.status ( HttpStatus.CREATED )
                .body ( otherService.createOtherService (
                        otherServiceRequest,
                        userId,
                        userRole,
                        autoAddaId,
                        companyLogo,
                        vatCertificate,
                        tradeLicenseAttachment,
                        companyImages
                ) );
    }

    @GetMapping("/all")
    public ResponseEntity<List<OtherService>> getAllOtherServices() {
        return ResponseEntity.ok ( otherService.getAllOtherServices ( ) );
    }

    @GetMapping
    public ResponseEntity<List<OtherService>> getAllOtherServicesByOwnerId(
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return ResponseEntity.ok ( otherService.getAllOtherServicesByOwnerId ( autoAddaId ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OtherService> getOtherServiceById(@PathVariable Integer id) {
        return otherService.getOtherServiceById ( id )
                .map ( ResponseEntity::ok )
                .orElse ( ResponseEntity.notFound ( ).build ( ) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<OtherService> updateOtherService(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody OtherServiceRequest otherServiceRequest
    ) {
        return ResponseEntity.ok ( otherService.updateOtherService ( id, otherServiceRequest ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOtherService(@PathVariable Integer id) {
        otherService.deleteOtherService ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping("/brandname/{name}")
    public ResponseEntity<List<OtherService>> searchByBrandName(@PathVariable String name) {
        return ResponseEntity.ok ( otherService.findOtherServicesByBrandName ( name ) );
    }

    @GetMapping("/service/{serviceType}")
    public ResponseEntity<List<OtherService>> getOtherServicesByServiceType(@PathVariable String serviceType) {
        return ResponseEntity.ok ( otherService.findOtherServicesByServiceType ( serviceType ) );
    }
}
