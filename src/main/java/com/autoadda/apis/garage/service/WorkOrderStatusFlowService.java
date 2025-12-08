package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.util.JobCardStatus;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkOrderStatusFlowService {

    // Define valid status transitions
    private static final Map<WorkOrderStatus, List<WorkOrderStatus>> VALID_TRANSITIONS = Map.ofEntries (
            Map.entry ( WorkOrderStatus.CREATED, Arrays.asList (
                    WorkOrderStatus.CONFIRMED,
                    WorkOrderStatus.NEGOTIATION,
                    WorkOrderStatus.CANCELLED,
                    WorkOrderStatus.REJECTED
            ) ),
            Map.entry ( WorkOrderStatus.NEGOTIATION, Arrays.asList (
                    WorkOrderStatus.CONFIRMED,
                    WorkOrderStatus.CANCELLED,
                    WorkOrderStatus.REJECTED
            ) ),
            Map.entry ( WorkOrderStatus.CONFIRMED, Arrays.asList (
                    WorkOrderStatus.ASSIGNED,
                    WorkOrderStatus.ON_HOLD,
                    WorkOrderStatus.CANCELLED
            ) ),

            Map.entry ( WorkOrderStatus.ASSIGNED, Arrays.asList (
                    WorkOrderStatus.WORK_IN_PROGRESS,
                    WorkOrderStatus.ON_HOLD,
                    WorkOrderStatus.CANCELLED
            ) ),

            Map.entry ( WorkOrderStatus.WORK_IN_PROGRESS, Arrays.asList (
                    WorkOrderStatus.WAITING_FOR_SPARES,
                    WorkOrderStatus.JOB_COMPLETED,
                    WorkOrderStatus.ON_HOLD,
                    WorkOrderStatus.CANCELLED
            ) ),

            Map.entry ( WorkOrderStatus.WAITING_FOR_SPARES, Arrays.asList (
                    WorkOrderStatus.WORK_IN_PROGRESS,
                    WorkOrderStatus.ON_HOLD,
                    WorkOrderStatus.CANCELLED
            ) ),

            Map.entry ( WorkOrderStatus.JOB_COMPLETED, Arrays.asList (
                    WorkOrderStatus.PENDING_REVIEW
            ) ),

            Map.entry ( WorkOrderStatus.PENDING_REVIEW, Arrays.asList (
                    WorkOrderStatus.INVOICE_INITIATED,
                    WorkOrderStatus.WORK_IN_PROGRESS // If rework needed
            ) ),


            Map.entry ( WorkOrderStatus.INVOICE_INITIATED, Arrays.asList (
                    WorkOrderStatus.INVOICED
            ) ),

            Map.entry ( WorkOrderStatus.INVOICED, Arrays.asList (
                    WorkOrderStatus.PENDING_PAYMENT,
                    WorkOrderStatus.PAYMENT_RECEIVED // If paid immediately
            ) ),

            Map.entry ( WorkOrderStatus.PENDING_PAYMENT, Arrays.asList (
                    WorkOrderStatus.PAYMENT_RECEIVED,
                    WorkOrderStatus.CANCELLED
            ) ),

            Map.entry ( WorkOrderStatus.PAYMENT_RECEIVED, Arrays.asList (
                    WorkOrderStatus.READY_FOR_DELIVERY
            ) ),

            Map.entry ( WorkOrderStatus.READY_FOR_DELIVERY, Arrays.asList (
                    WorkOrderStatus.DELIVERED_TO_CUSTOMER
            ) ),

            Map.entry ( WorkOrderStatus.DELIVERED_TO_CUSTOMER, Arrays.asList (
                    WorkOrderStatus.COMPLETED
            ) ),

            Map.entry ( WorkOrderStatus.ON_HOLD, Arrays.asList (
                    WorkOrderStatus.WORK_IN_PROGRESS,
                    WorkOrderStatus.CANCELLED
            ) )
    );
    private final WorkOrderRepository workOrderRepository;
    private final JobCardStatusFlowService jobCardStatusFlowService;
    private final SubscriptionUsageTrackingService subscriptionUsageTrackingService;

    public boolean isValidTransition(WorkOrderStatus currentStatus, WorkOrderStatus newStatus) {
        List<WorkOrderStatus> validNextStates = VALID_TRANSITIONS.get ( currentStatus );
        return validNextStates != null && validNextStates.contains ( newStatus );
    }

    public List<WorkOrderStatus> getValidNextStates(WorkOrderStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault ( currentStatus, Arrays.asList ( ) );
    }

    public boolean isTerminalStatus(WorkOrderStatus status) {
        return status == WorkOrderStatus.COMPLETED ||
                status == WorkOrderStatus.CANCELLED ||
                status == WorkOrderStatus.REJECTED;
    }

    public void updateWorkOrderStatusBasedOnJobCards(WorkOrder workOrder) {
        List<JobCard> jobCards = workOrder.getJobCards ( );

        if ( jobCards.isEmpty ( ) ) return;

        boolean allCompleted = jobCards.stream ( )
                .allMatch ( j -> j.getStatus ( ) == JobCardStatus.COMPLETED );
        boolean anyInProgress = jobCards.stream ( )
                .anyMatch ( jc -> jobCardStatusFlowService.isActiveWorkStatus ( jc.getStatus ( ) ) );

        // Update WorkOrder status logic
        if ( allCompleted && workOrder.getStatus ( ) != WorkOrderStatus.JOB_COMPLETED ) {
            workOrder.updateStatus ( WorkOrderStatus.JOB_COMPLETED );
            workOrder.setWorkCompletedDate ( LocalDateTime.now ( ) );
            workOrderRepository.save ( workOrder );

            // Track subscription usage when work is completed
            if (workOrder.getAppointment() != null) {
                JobCard firstJobCard = jobCards.isEmpty() ? null : jobCards.get(0);
                subscriptionUsageTrackingService.trackSubscriptionUsage(
                        workOrder.getAppointment(),
                        firstJobCard
                );
            }
        } else if ( anyInProgress && workOrder.getStatus ( ) == WorkOrderStatus.ASSIGNED ) {
            workOrder.updateStatus ( WorkOrderStatus.WORK_IN_PROGRESS );
            workOrderRepository.save ( workOrder );
        }
    }
}
