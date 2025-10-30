package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.AppointmentServiceRequest;
import com.autoadda.apis.garage.response.AppointmentServiceResponse;
import com.autoadda.apis.garage.service.AppointmentServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin("*")
@RestController
@RequestMapping("/appointment-services")
@RequiredArgsConstructor
public class AppointmentServiceController {
    private final AppointmentServiceService appointmentServiceService;

    @PostMapping
    public ResponseEntity<AppointmentServiceResponse> addAppointmentServices(@RequestBody AppointmentServiceRequest request) {
        return ResponseEntity.ok ( appointmentServiceService.createAppointmentService ( request ) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentServiceResponse> updateAppointmentServices(@PathVariable Integer id, @RequestBody AppointmentServiceRequest request) {
        return ResponseEntity.ok ( appointmentServiceService.updateAppointmentService ( id, request ) );
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<AppointmentServiceResponse>> getAppointmentServicesByAppointmentId(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok ( appointmentServiceService.getAppointmentServiceByAppointmentId ( appointmentId ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAppointmentService(@PathVariable Integer id) {
        appointmentServiceService.deleteAppointmentService ( id );
        return ResponseEntity.ok ( "Appointment service with id " + id + " deleted successfully." );
    }
}
