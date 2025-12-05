package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.AppointmentServices;
import com.autoadda.apis.garage.entity.GarageService;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.response.StatusTimelineEntry;
import com.autoadda.apis.garage.response.WorkOrderResponse;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkOrderMapper {

    private final AppointmentDetailMapper appointmentDetailMapper;
    private final JobCardMapper jobCardMapper;
    private final GarageMapper garageMapper;

    public WorkOrderResponse entityToResponse(WorkOrder save) {
        WorkOrderResponse workOrderResponse = new WorkOrderResponse ( );
        workOrderResponse.setId ( save.getId ( ) );
        log.info ( "12  + " + save.getId ( ) );

        log.info ( "save appointment details -----------------------------> " + save.getAppointment ( ).getId ( ) );
        workOrderResponse.setAppointmentDetail ( appointmentDetailMapper.entityToResponse ( save.getAppointment ( ) ) );
        workOrderResponse.setCreatedDate ( save.getCreatedDate ( ) );
        log.info ( "13-------------------------here is job cards size: " + save.getJobCards ( ).size ( ) );

        workOrderResponse.setJobCards ( save
                .getJobCards ( )
                .stream ( )
                .map ( jobCardMapper::entityToResponse )
                .toList ( ) );
        log.info ( "14 here is job cards size: " + workOrderResponse.getJobCards ( ).size ( ) );
        workOrderResponse.setGarage ( garageMapper.entityToResponse ( save.getGarage ( ) ) );

        workOrderResponse.setStatus ( save.getStatus ( ) );
        workOrderResponse.setTotalAmount ( save.getJobCards ( ).stream ( )
                .map ( JobCard::getService )
                .mapToDouble ( as -> {
                    double labour = as.getLabourPrice() != null ? as.getLabourPrice() : 0.0;
                    double price = as.getPrice() != null ? as.getPrice() : 0.0;
                    return labour + price;
                } ).sum ( ) );

        log.info ( "15" );
        workOrderResponse.setCustomerAcknowledged ( save.getCustomerAcknowledged ( ) );
        workOrderResponse.setGarageNegotiatedAmount1 ( save.getGarageNegotiatedAmount ( ) );
        workOrderResponse.setGarageNegotiatedComment ( save.getGarageNegotiatedComment ( ) );
        workOrderResponse.setCustomerNegotiatedAmount1 ( save.getCustomerNegotiatedAmount ( ) );
        workOrderResponse.setCustomerNegotiatedComment ( save.getCustomerNegotiatedComment ( ) );

        // Set status timestamps
        workOrderResponse.setStatusTimestamps ( save.getStatusTimestamps ( ) );

        // Generate status timeline
        workOrderResponse.setStatusTimeline ( generateStatusTimeline ( save.getStatusTimestamps ( ) ) );

        // Set delivery status fields for frontend
        workOrderResponse.setCurrentDeliveryStatus ( save.getCurrentDeliveryStatus ( ) );
        workOrderResponse.setCanInitiateDelivery ( save.canInitiateDelivery ( ) );

        return workOrderResponse;
    }

    private List<StatusTimelineEntry> generateStatusTimeline(Map<WorkOrderStatus, LocalDateTime> statusTimestamps) {
        if ( statusTimestamps == null || statusTimestamps.isEmpty ( ) ) {
            return new ArrayList<> ( );
        }

        List<StatusTimelineEntry> timeline = new ArrayList<> ( );

        // Convert map to sorted list by timestamp
        statusTimestamps.entrySet ( ).stream ( )
                .sorted ( Map.Entry.comparingByValue ( ) )
                .forEach ( entry -> {
                    StatusTimelineEntry timelineEntry = new StatusTimelineEntry ( );
                    timelineEntry.setStatus ( entry.getKey ( ) );
                    timelineEntry.setTimestamp ( entry.getValue ( ) );
                    timelineEntry.setDescription ( getStatusDescription ( entry.getKey ( ) ) );
                    timeline.add ( timelineEntry );
                } );

        // Calculate duration between statuses
        for (int i = 1; i < timeline.size ( ); i++) {
            StatusTimelineEntry current = timeline.get ( i );
            StatusTimelineEntry previous = timeline.get ( i - 1 );
            long durationInMinutes = ChronoUnit.MINUTES.between (
                    previous.getTimestamp ( ),
                    current.getTimestamp ( )
            );
            current.setDurationInMinutes ( (int) durationInMinutes );
        }

        return timeline;
    }

    private String getStatusDescription(WorkOrderStatus status) {
        switch (status) {
            case CREATED:
                return "Work order has been created";
            case NEGOTIATION:
                return "Negotiation with customer in progress";
            case CONFIRMED:
                return "Customer has confirmed the work order";
            case ASSIGNED:
                return "Work order assigned to technician";
            case WORK_IN_PROGRESS:
                return "Work has started on the vehicle";
            case WAITING_FOR_SPARES:
                return "Waiting for required parts/spares";
            case JOB_COMPLETED:
                return "All work has been completed";
            case PENDING_REVIEW:
                return "Quality check is pending";
            case READY_FOR_DELIVERY:
                return "Vehicle is ready for customer pickup";
            case INVOICE_INITIATED:
                return "Invoice generation has started";
            case INVOICED:
                return "Invoice has been generated";
            case PENDING_PAYMENT:
                return "Waiting for customer payment";
            case PAYMENT_RECEIVED:
                return "Payment has been received";
            case DELIVERED_TO_CUSTOMER:
                return "Vehicle delivered to customer";
            case COMPLETED:
                return "Work order has been completed";
            case ON_HOLD:
                return "Work order is temporarily on hold";
            case CANCELLED:
                return "Work order has been cancelled";
            case REJECTED:
                return "Work order has been rejected";
            default:
                return "Unknown status";
        }
    }
}
