package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.TechnicianAvailability;
import com.autoadda.apis.garage.request.TechnicianAvailabilityRequest;
import com.autoadda.apis.garage.service.TechnicianAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/technicians/availability")
public class TechnicianAvailabilityController {

    @Autowired
    private TechnicianAvailabilityService service;

    @PostMapping("/{technicianId}")
    public ResponseEntity<TechnicianAvailability> addAvailability(@RequestBody TechnicianAvailabilityRequest request,
                                                                  @PathVariable Integer technicianId) {

        if ( request.getTechnicianId ( ) == null || ! request.getTechnicianId ( ).equals ( technicianId ) ) {
            throw new IllegalArgumentException ( "Technician ID in request does not match path variable" );
        }
        request.setTechnicianId ( technicianId );
        if ( request.getStartTime ( ) == null || request.getEndTime ( ) == null ) {
            throw new IllegalArgumentException ( "Start time and end time must be provided" );
        }
        TechnicianAvailability availability = service.addAvailability ( request );
        return ResponseEntity.ok ( availability );
    }

    @GetMapping("/{technicianId}")
    public ResponseEntity<List<TechnicianAvailability>> getAvailability(@PathVariable Integer technicianId,
                                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok ( service.getAvailabilityForDate ( technicianId, date ) );
    }

    @GetMapping("/all/{technicianId}")
    public ResponseEntity<List<TechnicianAvailability>> getAllAvailabilityForTechnician(
            @PathVariable Integer technicianId) {
        return ResponseEntity.ok ( service.getAllAvailabilityForTechnician ( technicianId ) );
    }

    @GetMapping("/all")
    public ResponseEntity<List<TechnicianAvailability>> getAllAvailability() {
        return ResponseEntity.ok ( service.getAllAvailability ( ) );
    }

    @GetMapping("/all/date")
    public ResponseEntity<List<TechnicianAvailability>> getAllAvailabilityForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok ( service.getAllAvailabilityForDate ( date ) );
    }

    @GetMapping("/all/technician/{technicianId}/date")
    public ResponseEntity<List<TechnicianAvailability>> getAllAvailabilityForTechnicianAndDate(
            @PathVariable Integer technicianId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok ( service.getAvailabilityForDate ( technicianId, date ) );
    }
}