package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.AppointmentSlotRequest;
import com.autoadda.apis.garage.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slot")
public class SlotController {
    private final SlotService appointmentSlotService;

    @PostMapping
    public ResponseEntity<?> createSlot(@RequestBody AppointmentSlotRequest request) {
        return ResponseEntity.ok ( appointmentSlotService.createSlot ( request ) );
    }
    @GetMapping
    public ResponseEntity<?> getAllSlots() {
        return ResponseEntity.ok ( appointmentSlotService.getAllSlots ( ) );
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getSlotById(@PathVariable Integer id) {
        return ResponseEntity.ok ( appointmentSlotService.getSlotById ( id ) );
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSlot(@PathVariable Integer id, @RequestBody AppointmentSlotRequest request) {
        return ResponseEntity.ok ( appointmentSlotService.updateSlot ( id, request ) );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Integer id) {
        appointmentSlotService.deleteSlot ( id );
        return ResponseEntity.ok ( "Slot deleted successfully." );
    }
}
