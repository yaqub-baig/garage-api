package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.VehicleReceivedRequest;
import com.autoadda.apis.garage.response.VehicleRecievedResponse;
import com.autoadda.apis.garage.service.VehicleReceivedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicle-received")
@RequiredArgsConstructor
public class VehicleReceivedController {
    private final VehicleReceivedService vehicleReceivedService;

    @PostMapping
    public ResponseEntity<VehicleRecievedResponse> recievedVehicle(@RequestBody VehicleReceivedRequest request) {
        VehicleRecievedResponse response = vehicleReceivedService.receiveVehicle (request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{carRecievedId}")
    public ResponseEntity<VehicleRecievedResponse> getVehicleReceivedDetails(@PathVariable Integer carRecievedId) {
        VehicleRecievedResponse response = vehicleReceivedService.getVehicleReceivedDetails(carRecievedId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<List<VehicleRecievedResponse>> getVehicleReceivedByAppointmentId(@PathVariable Integer garageId) {
        List<VehicleRecievedResponse> response = vehicleReceivedService.getAllRecievedCarByGarageId(garageId);
        return ResponseEntity.ok(response);
    }
}
