package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.exception.GarageNotFoundException;
import com.autoadda.apis.garage.request.AppointmentStatusRequest;
import com.autoadda.apis.garage.request.CustomerAppointmentRequest;
import com.autoadda.apis.garage.response.AppointmentDetailResponse;
import com.autoadda.apis.garage.response.AppointmentResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.AppointmentService;
import com.autoadda.apis.garage.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentSlotService appointmentSlotService;


    @PostMapping
    public ResponseEntity<GenericResponse> createCustomerAppointment(@RequestBody CustomerAppointmentRequest request) {
        AppointmentDetails appointment = appointmentService.createCustomerAppointment ( request );
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( GenericResponse.success ( appointment ) );
    }

    @PostMapping("/{garageId}/book")
    public ResponseEntity<?> bookAppointment(@PathVariable Integer garageId, @RequestParam Integer vehicleId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId,
                                             @RequestParam(required = false) String selectedGarageServiceIds) {
        try {
            LocalDate date = time.toLocalDate ( );
            LocalTime timestamp = time.toLocalTime ( );
            AppointmentDetailResponse booking = appointmentSlotService.bookSlot ( garageId, vehicleId, date, timestamp, autoAddaId, selectedGarageServiceIds );
            return ResponseEntity.ok ( booking );
        }
        catch ( IllegalStateException e ) {
            return ResponseEntity.status ( HttpStatus.CONFLICT ).body ( e.getMessage ( ) );
        }
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getGarageAppointments(@PathVariable Integer garageId,
                                                                 @RequestParam(required = false) String status) throws GarageNotFoundException {
        List<AppointmentDetailResponse> appointments = appointmentService.getAppointmentsByGarage ( garageId, status );
        return ResponseEntity.ok ( GenericResponse.success ( appointments ) );
    }

    @PutMapping("/status")
    public ResponseEntity<GenericResponse> updateAppointmentStatus(@RequestBody AppointmentStatusRequest request) {
        AppointmentDetails updated = appointmentService.updateAppointmentStatus ( request );
        return ResponseEntity.ok ( GenericResponse.success ( updated ) );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<GenericResponse> getAppointmentDetails(@PathVariable Integer appointmentId) {
        AppointmentResponse response = appointmentService.getAppointmentsDetails ( appointmentId );
        return ResponseEntity.ok ( GenericResponse.success ( response ) );
    }
//TODO:33
    @GetMapping("/{garageId}/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(@PathVariable Integer garageId,
                                                                 @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate date) {

        // Use the new method that returns detailed slot information
        List<Map<String, Object>> slots = appointmentSlotService.getAvailableSlotsWithDetails ( garageId, date );

        Map<String, Object> res = new HashMap<> ( );
        res.put ( "garageId", garageId );
        res.put ( "date", date );
        res.put ( "availableSlots", slots );
        return ResponseEntity.ok ( res );
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<List<AppointmentDetailResponse>> getUserBookings(@RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        return ResponseEntity.ok ( appointmentSlotService.getBookingsByUser ( userId ) );
    }

    @DeleteMapping("/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Integer appointmentId, @RequestHeader(value = "X-Auto-Adda-Id") String userId) {
        appointmentSlotService.cancelBooking ( appointmentId );
        return ResponseEntity.ok ( "Booking cancelled successfully." );
    }

    @PutMapping("/edit/appointment")
    public ResponseEntity<?> editAppointment(@RequestParam(required = false) Integer garageId,
                                             @RequestParam(required = false) Integer vehicleId,
                                             @RequestParam Integer appointmentId,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        return appointmentSlotService.editBooking ( garageId, vehicleId, appointmentId, time, autoAddaId );
    }

}
