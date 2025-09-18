package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.RoadsideAssistance;
import com.autoadda.apis.garage.service.RoadsideAssistanceService;
import com.autoadda.apis.garage.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/roadside-assistance")
public class RoadsideAssistanceController {

    @Autowired
    private RoadsideAssistanceService assistanceService;

    @PostMapping
    public ResponseEntity<RoadsideAssistance> create(@RequestBody RoadsideAssistance request) {
        return ResponseEntity.ok ( assistanceService.createRequest ( request ) );
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<RoadsideAssistance>> getByOwner(@PathVariable Integer ownerId) {
        return ResponseEntity.ok ( assistanceService.getRequestsByOwner ( ownerId ) );
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RoadsideAssistance> updateStatus(@PathVariable Integer id, @RequestParam Status status,
                                                           @RequestParam(required = false) String remarks) {
        return ResponseEntity.ok ( assistanceService.updateStatus ( id, status, remarks ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadsideAssistance> getById(@PathVariable Integer id) {
        return assistanceService.getRequestById ( id ).map ( ResponseEntity::ok ).orElse ( ResponseEntity.notFound ( ).build ( ) );
    }
}
