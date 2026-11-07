package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.util.GaragePermission;
import com.autoadda.apis.garage.service.GarageResolver;
import com.autoadda.apis.garage.service.GarageAccessGuard;
import com.autoadda.apis.garage.request.VehicleReceivedRequest;
import com.autoadda.apis.garage.response.VehicleRecievedResponse;
import com.autoadda.apis.garage.service.VehicleReceivedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/vehicle-received")
@RequiredArgsConstructor
public class VehicleReceivedController {
    private final VehicleReceivedService vehicleReceivedService;
    private final GarageAccessGuard guard;
    private final GarageResolver garageResolver;

    @PostMapping
    public ResponseEntity<VehicleRecievedResponse> recievedVehicle(@RequestBody VehicleReceivedRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.require(caller, garageResolver.ofAppointment(request.getAppointmentId()), GaragePermission.VEHICLE_CHECKIN_DELIVERY);
        VehicleRecievedResponse response = vehicleReceivedService.receiveVehicle(request);
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

    @PutMapping("/update/{vehicleRecievedId}")
    public ResponseEntity<VehicleRecievedResponse> updateVehicleRecieved(@PathVariable Integer vehicleRecievedId, @RequestBody VehicleReceivedRequest request,
            @RequestHeader(value = "X-Auto-Adda-Id", required = false) String caller) {
        guard.require(caller, garageResolver.ofVehicleReceived(vehicleRecievedId), GaragePermission.VEHICLE_CHECKIN_DELIVERY);
        VehicleRecievedResponse response = vehicleReceivedService.updateVehicleReceived(vehicleRecievedId, request);
        return ResponseEntity.ok(response);
    }

}
