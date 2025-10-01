package com.autoadda.apis.garage.controller.os;

import com.autoadda.apis.garage.entity.os.OsAvailableService;
import com.autoadda.apis.garage.request.os.OsAssignServiceRequest;
import com.autoadda.apis.garage.response.os.OsGenericResponse;
import com.autoadda.apis.garage.response.os.OsJobCardResponse;
import com.autoadda.apis.garage.service.os.OsAvailableServiceService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/os-available-services")

public class OsAvailableServiceController {

    private final OsAvailableServiceService service;

    public OsAvailableServiceController(OsAvailableServiceService service) {
        this.service = service;
    }

    // Get all services
    @GetMapping
    public ResponseEntity<List<OsAvailableService>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<OsGenericResponse> assignTechnician(@RequestBody OsAvailableService request) {
        OsAvailableService osAvailableService = service.create(request);
        return ResponseEntity.ok(OsGenericResponse.success("OS Available service created", osAvailableService));
    }
    // Get one service by ID
    @GetMapping("/{id}")
    public ResponseEntity<OsAvailableService> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Update service
    @PutMapping("/{id}")
    public ResponseEntity<OsGenericResponse> update(
            @PathVariable Integer id,
            @RequestBody OsAvailableService updatedService) {
        service.update(id, updatedService);
        return ResponseEntity.ok(OsGenericResponse.success("Os service Updated Successfully", updatedService));
    }
}
