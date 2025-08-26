package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.AppointmentSlotRequest;
import com.autoadda.apis.garage.response.AppointmentSlotResponse;
import com.autoadda.apis.garage.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slot")
public class SlotController {
    private final SlotService appointmentSlotService;

    @PostMapping
    public ResponseEntity<AppointmentSlotResponse> createSlot(@RequestBody AppointmentSlotRequest request) {
        return ResponseEntity.ok ( appointmentSlotService.createSlot ( request ) );
    }

    @GetMapping
    public ResponseEntity<List<AppointmentSlotResponse>> getAllSlots() {
        return ResponseEntity.ok ( appointmentSlotService.getAllSlots ( ) );
    }

    @GetMapping("/available-slots")
    public ResponseEntity<List<AppointmentSlotResponse>> getAvailableSlots(@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                               @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime) {
        return ResponseEntity.ok ( appointmentSlotService.getAvailableSlots ( startTime, endTime ) );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentSlotResponse> getSlotById(@PathVariable Integer id) {
        return ResponseEntity.ok ( appointmentSlotService.getSlotById ( id ) );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentSlotResponse> updateSlot(@PathVariable Integer id, @RequestBody AppointmentSlotRequest request) {
        return ResponseEntity.ok ( appointmentSlotService.updateSlot ( id, request ) );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Integer id) {
        appointmentSlotService.deleteSlot ( id );
        return ResponseEntity.ok ( "Slot deleted successfully." );
    }
}
