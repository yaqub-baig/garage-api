package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.TechnicianRequest;
import com.autoadda.apis.garage.response.TechnicianResponse;
import com.autoadda.apis.garage.service.TechnicianService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/technicians")
@RequiredArgsConstructor
public class TechnicianController {
    private final TechnicianService technicianService;

    @PostMapping
    public ResponseEntity<TechnicianResponse> addTechnician(@RequestBody TechnicianRequest request) {
        TechnicianResponse created = technicianService.onboardTechnician ( request );
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( created );
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<List<TechnicianResponse>> getByGarage(@PathVariable Integer garageId) {
        return ResponseEntity.ok ( technicianService.getTechniciansByGarage ( garageId ) );
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<TechnicianResponse> getByEmail(@PathVariable String email) {
        return technicianService.findByEmail ( email )
                .map ( ResponseEntity::ok )
                .orElse ( ResponseEntity.notFound ( ).build ( ) );
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<TechnicianResponse>> getBySkill(@PathVariable Integer skillId) {
        List<TechnicianResponse> technicians = technicianService.findBySkillSet ( skillId );
        if ( technicians.isEmpty ( ) ) {
            return ResponseEntity.notFound ( ).build ( );
        }
        return ResponseEntity.ok ( technicians );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TechnicianResponse>> getByStatus(@PathVariable String status) {
        List<TechnicianResponse> technicians = technicianService.findByStatus ( status );
        if ( technicians.isEmpty ( ) ) {
            return ResponseEntity.notFound ( ).build ( );
        }
        return ResponseEntity.ok ( technicians );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnicianResponse> getById(@PathVariable Integer id) {
        return technicianService.findById ( id )
                .map ( ResponseEntity::ok )
                .orElse ( ResponseEntity.notFound ( ).build ( ) );
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Integer id) {
        technicianService.deleteTechnician ( id );
        return ResponseEntity.noContent ( ).build ( );
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<TechnicianResponse> updateTechnician(@PathVariable Integer id, @RequestBody TechnicianRequest request) {
        return new ResponseEntity<> ( technicianService.updateTechnician ( id, request ), HttpStatus.OK );

    }
    // Additional endpoints can be added as needed
}
