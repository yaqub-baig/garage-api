package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.CreatePartRequestRequest;
import com.autoadda.apis.garage.request.CustomerPartResponseRequest;
import com.autoadda.apis.garage.request.GarageProcurementRequest;
import com.autoadda.apis.garage.request.JobCardStatusRequest;
import com.autoadda.apis.garage.request.UpdatePartRequestRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.JobCardResponse;
import com.autoadda.apis.garage.response.PartRequestResponse;
import com.autoadda.apis.garage.service.JobCardService;
import com.autoadda.apis.garage.service.PartRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/part-requests")
@RequiredArgsConstructor
@Slf4j
public class PartRequestController {

    private final PartRequestService partRequestService;
    private final JobCardService jobCardService;

    /**
     * Create a part request (can also be done through job card status update)
     */
    @PostMapping
    public ResponseEntity<GenericResponse> createPartRequest(
            @RequestBody CreatePartRequestRequest request) {
        log.info("Creating part request for job card: {}", request.getJobCardId());
        PartRequestResponse response = partRequestService.createPartRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Part request created successfully", response));
    }

    /**
     * Update job card status with part request (for WAITING_FOR_PARTS status)
     */
    @PutMapping("/jobcard/status")
    public ResponseEntity<GenericResponse> updateJobCardStatusWithPartRequest(
            @RequestBody JobCardStatusRequest request) {
        log.info("Updating job card status with part request: {}", request.getJobcardId());
        JobCardResponse response = jobCardService.updateJobCardStatusWithPartRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Job card status updated successfully", response));
    }

    /**
     * Get part request by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> getPartRequestById(@PathVariable Integer id) {
        log.info("Fetching part request by ID: {}", id);
        PartRequestResponse response = partRequestService.getPartRequestById(id);
        return ResponseEntity.ok(GenericResponse.success("Part request retrieved successfully", response));
    }

    /**
     * Update basic details of a part request (notes and/or items)
     */
    @PutMapping("/{id}")
    public ResponseEntity<GenericResponse> updatePartRequest(
            @PathVariable Integer id,
            @RequestBody UpdatePartRequestRequest request) {
        log.info("Updating part request: {}", id);
        PartRequestResponse response = partRequestService.updatePartRequest(id, request);
        return ResponseEntity.ok(GenericResponse.success("Part request updated successfully", response));
    }

    /**
     * Get part request by job card ID
     */
    @GetMapping("/job/{jobCardId}")
    public ResponseEntity<GenericResponse> getPartRequestByJobCardId(
            @PathVariable Integer jobCardId) {
        log.info("Fetching part request for job card: {}", jobCardId);
        PartRequestResponse response = partRequestService.getPartRequestByJobCardId(jobCardId);
        return ResponseEntity.ok(GenericResponse.success("Part request retrieved successfully", response));
    }

    /**
     * Get all part requests for a customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<GenericResponse> getPartRequestsByCustomerId(
            @PathVariable Integer customerId) {
        log.info("Fetching part requests for customer: {}", customerId);
        List<PartRequestResponse> responses = partRequestService.getPartRequestsByCustomerId(customerId);
        return ResponseEntity.ok(GenericResponse.success(
                "Part requests retrieved successfully", responses));
    }

    /**
     * Get pending part requests for a customer (requires response)
     */
    @GetMapping("/customer/{customerId}/pending")
    public ResponseEntity<GenericResponse> getPendingPartRequestsByCustomerId(
            @PathVariable Integer customerId) {
        log.info("Fetching pending part requests for customer: {}", customerId);
        List<PartRequestResponse> responses = partRequestService.getPendingPartRequestsByCustomerId(customerId);
        return ResponseEntity.ok(GenericResponse.success(
                "Pending part requests retrieved successfully", responses));
    }

    /**
     * Get part requests by garage ID
     */
    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getPartRequestsByGarageId(
            @PathVariable Integer garageId) {
        log.info("Fetching part requests for garage: {}", garageId);
        List<PartRequestResponse> responses = partRequestService.getPartRequestsByGarageId(garageId);
        return ResponseEntity.ok(GenericResponse.success(
                "Part requests retrieved successfully", responses));
    }

    /**
     * Customer submits their choice for part procurement
     */
    @PutMapping("/{id}/customer-response")
    public ResponseEntity<GenericResponse> submitCustomerResponse(
            @PathVariable Integer id,
            @RequestBody CustomerPartResponseRequest request) {
        log.info("Customer submitting response for part request: {}", id);
        PartRequestResponse response = partRequestService.submitCustomerResponse(id, request);
        return ResponseEntity.ok(GenericResponse.success(
                "Customer response submitted successfully", response));
    }

    /**
     * Garage adds procurement details (automatically adds to invoice)
     */
    @PostMapping("/{id}/procurement")
    public ResponseEntity<GenericResponse> addGarageProcurement(
            @PathVariable Integer id,
            @RequestBody GarageProcurementRequest request) {
        log.info("Adding garage procurement for part request: {}", id);

        // Set the part request ID if not already set
        if (request.getPartRequestItemId() == null) {
            log.warn("Part request item ID not provided in request");
        }

        PartRequestResponse response = partRequestService.addGarageProcurement(request);
        return ResponseEntity.ok(GenericResponse.success(
                "Part procurement added and invoice updated successfully", response));
    }

    /**
     * Mark part as received when customer delivers it
     */
    @PutMapping("/{id}/mark-received")
    public ResponseEntity<GenericResponse> markPartReceived(@PathVariable Integer id) {
        log.info("Marking part as received for part request: {}", id);
        PartRequestResponse response = partRequestService.markPartReceived(id);
        return ResponseEntity.ok(GenericResponse.success(
                "Part marked as received successfully", response));
    }
}
