package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.WorkOrderRequest;
import com.autoadda.apis.garage.response.StatusTimelineEntry;
import com.autoadda.apis.garage.response.VehicleServiceHistoryResponse;
import com.autoadda.apis.garage.response.WorkOrderResponse;
import com.autoadda.apis.garage.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Order Management", description = "APIs for managing work orders and service history")
public class WorkOrderController {
    private final WorkOrderService workOrderService;


    @PostMapping
    public ResponseEntity<WorkOrderResponse> createWorkOrder(@RequestBody WorkOrderRequest request) {
        WorkOrderResponse workOrder = workOrderService.createWorkOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(workOrder);
    }

    @GetMapping("/workorder-by/{appointmentId}")
    public ResponseEntity<WorkOrderResponse> getWorkOrderByAppointmentId(@PathVariable Integer appointmentId) {
        WorkOrderResponse workOrder = workOrderService.getWorkOrderByAppointmentId(appointmentId);
        return ResponseEntity.ok(workOrder);
    }

    @PutMapping("/update/{workOrderId}")
    public ResponseEntity<WorkOrderResponse> updateWorkOrder(@PathVariable Integer workOrderId, @RequestBody WorkOrderRequest request) {
        WorkOrderResponse response = workOrderService.updateWorkOrder(workOrderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-garage/{garageId}")
    public ResponseEntity<List<WorkOrderResponse>> getWorkOrdersByGarageId(@PathVariable Integer garageId) {
        List<WorkOrderResponse> workOrder = workOrderService.getWorkOrdersByGarageId(garageId);
        return ResponseEntity.ok(workOrder);
    }

    @Operation(
            summary = "Get vehicle service history",
            description = "Retrieves the complete service history for a specific vehicle, including all delivered/completed work orders with services performed, garage information, invoice details, and delivery information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved vehicle service history",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VehicleServiceHistoryResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vehicle not found"
            )
    })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<VehicleServiceHistoryResponse>> getWorkOrdersByVehicleId(
            @Parameter(description = "ID of the vehicle to retrieve service history for", required = true)
            @PathVariable Integer vehicleId) {
        List<VehicleServiceHistoryResponse> workOrders = workOrderService.getWorkOrdersByVehicleId(vehicleId);
        return ResponseEntity.ok(workOrders);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<WorkOrderResponse>> getWorkOrdersByCustomerId(@PathVariable Integer customerId) {
        List<WorkOrderResponse> workOrders = workOrderService.getWorkOrdersByCustomerId(customerId);
        return ResponseEntity.ok(workOrders);
    }

    @GetMapping("/{workOrderId}/timeline")
    public ResponseEntity<List<StatusTimelineEntry>> getWorkOrderTimeline(@PathVariable Integer workOrderId) {
        WorkOrderResponse workOrder = workOrderService.getWorkOrderResponseById(workOrderId);
        return ResponseEntity.ok(workOrder.getStatusTimeline());
    }
}
