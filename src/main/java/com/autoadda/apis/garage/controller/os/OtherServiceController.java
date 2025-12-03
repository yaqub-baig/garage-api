package com.autoadda.apis.garage.controller.os;


import com.autoadda.apis.garage.entity.os.OtherService;
import com.autoadda.apis.garage.request.os.OtherServiceRequest;
import com.autoadda.apis.garage.response.os.OsResponse;
import com.autoadda.apis.garage.service.os.OtherServiceManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/other-services")
@RequiredArgsConstructor
public class OtherServiceController {

    private final OtherServiceManagementService otherService;

    @PostMapping
    public ResponseEntity<OtherService> createOtherService(
            @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
            @RequestBody OtherServiceRequest otherServiceRequest) {
        return ResponseEntity.status ( HttpStatus.CREATED )
                .body ( otherService.createOtherService (
                        otherServiceRequest,
                        autoAddaId
                ) );
    }

    @GetMapping("/all")
    public ResponseEntity<List<OtherService>> getAllOtherServices() {
        return ResponseEntity.ok ( otherService.getAllOtherServices ( ) );
    }

    @GetMapping
    public ResponseEntity<List<OtherService>> getAllOtherServicesByOwnerId(
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

    @GetMapping("/search")
    public ResponseEntity<List<OsResponse>> searchOsServices( @RequestParam(required = false) String city, @RequestParam(required = false) String zip, @RequestParam(required = false) String garageName, @RequestParam(required = false) List<Integer> availableServiceIds) {
        return ResponseEntity.ok ( otherService.searchOtherServices(  city, zip, garageName, availableServiceIds ) );
    }
}
