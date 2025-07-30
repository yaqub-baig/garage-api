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
import java.util.HashMap;
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

    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OtherService> updateOtherService(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestPart(value = "otherService", required = false) OtherServiceRequest otherServiceRequest,
            @RequestPart(value = "companyLogo", required = false) MultipartFile companyLogo,
            @RequestPart(value = "vatCertificate", required = false)
            @Size(max = 5, message = "Maximum 5 VAT certificate files allowed")
            List<MultipartFile> vatCertificate,
            @RequestPart(value = "tradeLicenseAttachment", required = false)
            @Size(max = 5, message = "Maximum 5 trade license files allowed")
            List<MultipartFile> tradeLicenseAttachment,
            @RequestPart(value = "companyImages", required = false)
            @Size(max = 10, message = "Maximum 10 company images allowed")
            List<MultipartFile> companyImages) throws IOException {

        HashMap<String, Object> requestData = new HashMap<> ( );
        if ( otherServiceRequest != null )
            requestData.put ( "data", otherServiceRequest );
        if ( tradeLicenseAttachment != null && ! tradeLicenseAttachment.isEmpty ( ) )
            requestData.put ( "tradeLicenseAttachment", tradeLicenseAttachment );
        if ( vatCertificate != null && ! vatCertificate.isEmpty ( ) )
            requestData.put ( "vatCertificate", vatCertificate );
        if ( companyImages != null && ! companyImages.isEmpty ( ) )
            requestData.put ( "companyImages", companyImages );
        if ( companyLogo != null && ! companyLogo.isEmpty ( ) )
            requestData.put ( "companyLogo", companyLogo );

        return ResponseEntity.ok ( otherService.updateOtherService ( id, requestData ) );
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
