package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.RequestingGarageTechnicianRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.RequestingGarageTechnicianResponse;
import com.autoadda.apis.garage.service.RequestingGarageTechnicianService;
import com.autoadda.apis.garage.util.SendBy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing mutual consent requests between garages and technicians
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/requestingGarageTechnician")
@RequiredArgsConstructor
@Slf4j
public class RequestingGarageTechnicianController {

    private final RequestingGarageTechnicianService service;

    // ====================================================================================
    // SEND REQUEST
    // ====================================================================================

    /**
     * Send a collaboration request from garage to technician
     */
    @PostMapping("/garage/send")
    public ResponseEntity<GenericResponse> sendRequestFromGarage(
            @Valid @RequestBody RequestingGarageTechnicianRequest request,
            @RequestParam String garageId) {

        log.info("Garage {} sending request to technician {}", garageId, request.getTechnicianId());

        // Validate that the garage in the request matches the authenticated garage
        if (!request.getGarageId().equals(Integer.parseInt(garageId))) {
            return ResponseEntity.badRequest().body(
                GenericResponse.failed("Garage ID in request must match authenticated garage", null));
        }

        RequestingGarageTechnicianResponse response = service.sendRequest(request, SendBy.GARAGE);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request sent successfully to technician", response));
    }

    /**
     * Send a collaboration request from technician to garage
     */
    @PostMapping("/technician/send")
    public ResponseEntity<GenericResponse> sendRequestFromTechnician(
            @Valid @RequestBody RequestingGarageTechnicianRequest request,
             @RequestParam String technicianId) {

        log.info("Technician {} sending request to garage {}", technicianId, request.getGarageId());

        // Validate that the technician in the request matches the authenticated technician
        if (!request.getTechnicianId().equals(Integer.parseInt(technicianId))) {
            return ResponseEntity.badRequest().body(
                GenericResponse.failed("Technician ID in request must match authenticated technician", null));
        }

        RequestingGarageTechnicianResponse response = service.sendRequest(request, SendBy.TECHNICIAN);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request sent successfully to garage", response));
    }

    // ====================================================================================
    // APPROVE/REJECT - Garage Actions
    // ====================================================================================

    /**
     * Garage approves a pending request from a technician
     */
    @PutMapping("/garage/{requestId}/approve")
    public ResponseEntity<GenericResponse> garageApproveRequest(
            @PathVariable Integer requestId,
            @RequestParam String garageId) {

        log.info("Garage {} approving request {}", garageId, requestId);

        RequestingGarageTechnicianResponse response = service.approveRequest(
                requestId,
                Integer.parseInt(garageId),
                SendBy.GARAGE);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request approved successfully. Technician has been added to your garage.", response));
    }

    /**
     * Garage rejects a pending request from a technician
     */
    @PutMapping("/garage/{requestId}/reject")
    public ResponseEntity<GenericResponse> garageRejectRequest(
            @PathVariable Integer requestId,
            @RequestParam String garageId) {

        log.info("Garage {} rejecting request {}", garageId, requestId);

        RequestingGarageTechnicianResponse response = service.rejectRequest(
                requestId,
                Integer.parseInt(garageId),
                SendBy.GARAGE);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request rejected successfully", response));
    }

    // ====================================================================================
    // APPROVE/REJECT - Technician Actions
    // ====================================================================================

    /**
     * Technician approves a pending request from a garage
     */
    @PutMapping("/technician/{requestId}/approve")
    public ResponseEntity<GenericResponse> technicianApproveRequest(
            @PathVariable Integer requestId,
            @RequestParam String technicianId) {

        log.info("Technician {} approving request {}", technicianId, requestId);

        RequestingGarageTechnicianResponse response = service.approveRequest(
                requestId,
                Integer.parseInt(technicianId),
                SendBy.TECHNICIAN);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request approved successfully. You have been added to the garage.", response));
    }

    /**
     * Technician rejects a pending request from a garage
     */
    @PutMapping("/technician/{requestId}/reject")
    public ResponseEntity<GenericResponse> technicianRejectRequest(
            @PathVariable Integer requestId,
            @RequestParam String technicianId) {

        log.info("Technician {} rejecting request {}", technicianId, requestId);

        RequestingGarageTechnicianResponse response = service.rejectRequest(
                requestId,
                Integer.parseInt(technicianId),
                SendBy.TECHNICIAN);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request rejected successfully", response));
    }

    // ====================================================================================
    // QUERY - Pending Requests (Action Items)
    // ====================================================================================

    /**
     * Get all pending requests for a garage (sent by technicians)
     */
    @GetMapping("/garage/{garageId}/pending")
    public ResponseEntity<GenericResponse> getPendingRequestsForGarage(
            @PathVariable Integer garageId) {

        log.info("Fetching pending requests for garage {}", garageId);

        List<RequestingGarageTechnicianResponse> requests =
                service.getPendingRequestsForGarage(garageId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Pending requests retrieved successfully", requests));
    }

    /**
     * Get all pending requests for a technician (sent by garages)
     */
    @GetMapping("/technician/{technicianId}/pending")
    public ResponseEntity<GenericResponse> getPendingRequestsForTechnician(
            @PathVariable Integer technicianId) {

        log.info("Fetching pending requests for technician {}", technicianId);

        List<RequestingGarageTechnicianResponse> requests =
                service.getPendingRequestsForTechnician(technicianId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Pending requests retrieved successfully", requests));
    }

    // ====================================================================================
    // QUERY - Request History (Audit Trail)
    // ====================================================================================

    /**
     * Get all requests for a garage (all statuses) - for audit trail
     */
    @GetMapping("/garage/{garageId}/history")
    public ResponseEntity<GenericResponse> getRequestHistoryForGarage(
            @PathVariable Integer garageId) {

        log.info("Fetching request history for garage {}", garageId);

        List<RequestingGarageTechnicianResponse> requests =
                service.getRequestHistoryForGarage(garageId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request history retrieved successfully", requests));
    }

    /**
     * Get all requests for a technician (all statuses) - for audit trail
     */
    @GetMapping("/technician/{technicianId}/history")
    public ResponseEntity<GenericResponse> getRequestHistoryForTechnician(
            @PathVariable Integer technicianId) {

        log.info("Fetching request history for technician {}", technicianId);

        List<RequestingGarageTechnicianResponse> requests =
                service.getRequestHistoryForTechnician(technicianId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Request history retrieved successfully", requests));
    }
}
