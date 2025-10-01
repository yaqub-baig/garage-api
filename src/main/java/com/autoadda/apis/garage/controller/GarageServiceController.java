package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.request.GarageServiceRequest;
import com.autoadda.apis.garage.response.GarageServiceResponse;
import com.autoadda.apis.garage.service.GarageServiceManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/garage-services")
@RequiredArgsConstructor
public class GarageServiceController {

    private final GarageServiceManagementService garageServiceService;

    @PostMapping
    public ResponseEntity<GarageService> create(@RequestBody GarageServiceRequest garageService) {
        return ResponseEntity.ok ( garageServiceService.createGarageService ( garageService ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<GarageService>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok ( garageServiceService.getGarageServiceById ( id ) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<GarageService> update(@PathVariable Integer id, @RequestBody GarageService updated) {
        return ResponseEntity.ok ( garageServiceService.updateGarageService ( id, updated ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        garageServiceService.deleteGarageService ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @GetMapping
    public ResponseEntity<List<GarageService>> getAll() {
        return ResponseEntity.ok ( garageServiceService.getAllGarageServices ( ) );
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<List<GarageServiceResponse>> getByGarage(@PathVariable Integer garageId) {
        return ResponseEntity.ok ( garageServiceService.getByGarageId ( garageId ) );
    }
}
