package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.CustomerDeliveryApprovalRequest;
import com.autoadda.apis.garage.request.VehicleDeliveryRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.VehicleDeliveryComparisonResponse;
import com.autoadda.apis.garage.response.VehicleDeliveryResponse;
import com.autoadda.apis.garage.response.VehicleDeliveryWithReceivedChecklistResponse;
import com.autoadda.apis.garage.service.VehicleDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/vehicle-delivery")
@RequiredArgsConstructor
@Slf4j
public class VehicleDeliveryController {

    private final VehicleDeliveryService vehicleDeliveryService;

    /**
     * Initiate delivery process for a work order
     */
    @PostMapping("/workorder/{workOrderId}/initiate")
    public ResponseEntity<GenericResponse> initiateDelivery(
            @PathVariable Integer workOrderId,
            @RequestParam Integer technicianId) {
        log.info("API: Initiate delivery for work order: {}, technician: {}", workOrderId, technicianId);
        VehicleDeliveryResponse response = vehicleDeliveryService.initiateDelivery(workOrderId, technicianId);
        return ResponseEntity.ok(GenericResponse.success("Delivery initiated successfully", response));
    }

    /**
     * Submit delivery checklist with item verifications
     */
    @PutMapping("/{deliveryId}/checklist")
    public ResponseEntity<GenericResponse> submitDeliveryChecklist(
            @PathVariable Integer deliveryId,
            @RequestBody VehicleDeliveryRequest request) {
        log.info("API: Submit delivery checklist for delivery ID: {}", deliveryId);
        VehicleDeliveryResponse response = vehicleDeliveryService.submitDeliveryChecklist(deliveryId, request);
        return ResponseEntity.ok(GenericResponse.success("Delivery checklist submitted successfully", response));
    }

    /**
     * Get comparison between receiving and delivery checklists
     */
    @GetMapping("/workorder/{workOrderId}/comparison")
    public ResponseEntity<GenericResponse> getDeliveryComparison(@PathVariable Integer workOrderId) {
        log.info("API: Get delivery comparison for work order: {}", workOrderId);
        VehicleDeliveryComparisonResponse response = vehicleDeliveryService.getDeliveryComparison(workOrderId);
        return ResponseEntity.ok(GenericResponse.success("Delivery comparison retrieved successfully", response));
    }

    /**
     * Customer approves or rejects delivery
     */
    @PutMapping("/{deliveryId}/customer-approval")
    public ResponseEntity<GenericResponse> submitCustomerApproval(
            @PathVariable Integer deliveryId,
            @RequestBody CustomerDeliveryApprovalRequest request) {
        log.info("API: Customer approval for delivery ID: {}. Approved: {}", deliveryId, request.getApproved());
        VehicleDeliveryResponse response = vehicleDeliveryService.submitCustomerApproval(deliveryId, request);

        String message = Boolean.TRUE.equals(request.getApproved()) ?
                "Delivery approved successfully. Vehicle delivered to customer." :
                "Delivery rejected. Please resolve issues and re-initiate delivery.";

        return ResponseEntity.ok(GenericResponse.success(message, response));
    }

    /**
     * Get delivery details by work order ID
     */
    @GetMapping("/workorder/{workOrderId}")
    public ResponseEntity<GenericResponse> getDeliveryByWorkOrderId(@PathVariable Integer workOrderId) {
        log.info("API: Get delivery details for work order: {}", workOrderId);
        VehicleDeliveryWithReceivedChecklistResponse response = vehicleDeliveryService.getDeliveryByWorkOrderId(workOrderId);
        return ResponseEntity.ok(GenericResponse.success("Delivery details retrieved successfully", response));
    }

    /**
     * Get delivery details by delivery ID
     */
    @GetMapping("/{deliveryId}")
    public ResponseEntity<GenericResponse> getDeliveryById(@PathVariable Integer deliveryId) {
        log.info("API: Get delivery details by ID: {}", deliveryId);
        VehicleDeliveryResponse response = vehicleDeliveryService.getDeliveryById(deliveryId);
        return ResponseEntity.ok(GenericResponse.success("Delivery details retrieved successfully", response));
    }
}
