package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.AppointmentDetails;
import com.autoadda.apis.garage.exception.GarageNotFoundException;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.request.AppointmentStatusRequest;
import com.autoadda.apis.garage.request.CustomerAppointmentRequest;
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
//
//	@PostMapping
//	public ResponseEntity<AppointmentDetails> createAppointment(@RequestBody AppointmentDetails appointmentDetails) {
//		AppointmentDetails saved = appointmentDetailsRepository.save(appointmentDetails);
//		return ResponseEntity.ok(saved);
//	}

    @PostMapping("/{garageId}/book")
    public ResponseEntity<?> bookAppointment(@PathVariable Integer garageId, @RequestParam Integer vehicleId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time,
                                             @RequestHeader(value = "X-Auto-Adda-Id") String autoAddaId) {
        try {
            LocalDate date = time.toLocalDate ( );
            LocalTime timestamp = time.toLocalTime ( );
            AppointmentDetails booking = appointmentSlotService.bookSlot ( garageId, vehicleId, date, timestamp );
            return ResponseEntity.ok ( booking );
        }
        catch ( IllegalStateException e ) {
            return ResponseEntity.status ( HttpStatus.CONFLICT ).body ( e.getMessage ( ) );
        }
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getGarageAppointments(@PathVariable Integer garageId,
                                                                 @RequestParam(required = false) String status) throws GarageNotFoundException {
        List<AppointmentDetails> appointments = appointmentService.getAppointmentsByGarage ( garageId, status );
        return ResponseEntity.ok ( GenericResponse.success ( appointments ) );
    }

    @PutMapping("/status")
    public ResponseEntity<GenericResponse> updateAppointmentStatus(@RequestBody AppointmentStatusRequest request) {
        AppointmentDetails updated = appointmentService.updateAppointmentStatus ( request );
        return ResponseEntity.ok ( GenericResponse.success ( "Appointment status updated" ) );
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<GenericResponse> getAppointmentDetails(@PathVariable Integer appointmentId) {
        AppointmentResponse response = appointmentService.getAppointmentsDetails ( appointmentId );
        return ResponseEntity.ok ( GenericResponse.success ( response ) );
    }

    @GetMapping("/{garageId}/available-slots")
    public ResponseEntity<Map<String, Object>> getAvailableSlots(@PathVariable Integer garageId,
                                                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<LocalTime> slots = appointmentSlotService.getAvailableSlots ( garageId, date );
        Map<String, Object> res = new HashMap<> ( );
        res.put ( "garageId", garageId );
        res.put ( "date", date );
        res.put ( "availableSlots", slots );
        return ResponseEntity.ok ( res );
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<List<AppointmentDetails>> getUserBookings(@RequestHeader(value = "X-User-Id") String userId) {
        return ResponseEntity.ok ( appointmentSlotService.getBookingsByUser ( userId ) );
    }

}
