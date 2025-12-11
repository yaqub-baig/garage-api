package com.autoadda.apis.garage.controller.os;


import com.autoadda.apis.garage.entity.os.OtherServiceBridge;
import com.autoadda.apis.garage.request.GarageServiceRequest;
import com.autoadda.apis.garage.service.os.OtherServiceBridgeManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/other-services-bridge")
@RequiredArgsConstructor
public class OtherServiceBridgeController {

    private final OtherServiceBridgeManagementService otherServiceBridgeManagementService;

    @PostMapping
    public ResponseEntity<OtherServiceBridge> create(@RequestBody GarageServiceRequest garageService) {
        return ResponseEntity.ok(otherServiceBridgeManagementService.createOtherServiceBridge(garageService));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<OtherServiceBridge>> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(otherServiceBridgeManagementService.getOtherServiceBridgeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OtherServiceBridge> update(@PathVariable Integer id, @RequestBody OtherServiceBridge updated) {
        return ResponseEntity.ok(otherServiceBridgeManagementService.updateOtherServiceBridge(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        otherServiceBridgeManagementService.deleteOtherServiceBridge(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<OtherServiceBridge>> getAll() {
        return ResponseEntity.ok(otherServiceBridgeManagementService.getAllOtherServiceBridges());
    }

    @GetMapping("/other-service/{garageId}")
    public ResponseEntity<List<OtherServiceBridge>> getByGarage(@PathVariable Integer garageId) {
        return ResponseEntity.ok(otherServiceBridgeManagementService.getByOtherServiceId(garageId));
    }
}

