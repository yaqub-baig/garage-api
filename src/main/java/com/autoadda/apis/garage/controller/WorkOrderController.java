package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.WorkOrderRequest;
import com.autoadda.apis.garage.response.StatusTimelineEntry;
import com.autoadda.apis.garage.response.WorkOrderResponse;
import com.autoadda.apis.garage.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService workOrderService;


    @PostMapping
    public ResponseEntity<WorkOrderResponse> createWorkOrder(@RequestBody WorkOrderRequest request) {
        WorkOrderResponse workOrder = workOrderService.createWorkOrder ( request );
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( workOrder );
    }

    @GetMapping("/workorder-by/{appointmentId}")
    public ResponseEntity<WorkOrderResponse> getWorkOrderByAppointmentId(@PathVariable Integer appointmentId) {
        WorkOrderResponse workOrder = workOrderService.getWorkOrderByAppointmentId ( appointmentId );
        return ResponseEntity.ok ( workOrder );
    }

    @PutMapping("/update/{workOrderId}")
    public ResponseEntity<WorkOrderResponse> updateWorkOrder(@PathVariable Integer workOrderId, @RequestBody WorkOrderRequest request) {
        WorkOrderResponse response = workOrderService.updateWorkOrder ( workOrderId, request );
        return ResponseEntity.ok ( response );
    }

    @GetMapping("/by-garage/{garageId}")
    public ResponseEntity<List<WorkOrderResponse>> getWorkOrdersByGarageId(@PathVariable Integer garageId) {
        List<WorkOrderResponse> workOrder = workOrderService.getWorkOrdersByGarageId ( garageId );
        return ResponseEntity.ok ( workOrder );
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<WorkOrderResponse>> getWorkOrdersByCustomerId(@PathVariable Integer customerId) {
        List<WorkOrderResponse> workOrders = workOrderService.getWorkOrdersByCustomerId ( customerId );
        return ResponseEntity.ok ( workOrders );
    }

    @GetMapping("/{workOrderId}/timeline")
    public ResponseEntity<List<StatusTimelineEntry>> getWorkOrderTimeline(@PathVariable Integer workOrderId) {
        WorkOrderResponse workOrder = workOrderService.getWorkOrderResponseById ( workOrderId );
        return ResponseEntity.ok ( workOrder.getStatusTimeline ( ) );
    }
}
