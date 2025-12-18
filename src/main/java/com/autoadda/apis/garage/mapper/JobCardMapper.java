package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.response.JobCardResponse;
import com.autoadda.apis.garage.response.JobCardStatusTimelineEntry;
import com.autoadda.apis.garage.response.TechnicianWorkHistoryResponse;
import com.autoadda.apis.garage.util.JobCardStatus;
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
public class JobCardMapper {
    private final AppointmentServicesMapper appointmentServicesMapper;

    public JobCardResponse entityToResponse(JobCard jobCard) {
        log.info ( "9" );
        JobCardResponse response = JobCardResponse.builder ( )
                .serviceStartTime ( jobCard.getServiceStartTime ( ) != null ? jobCard.getServiceStartTime ( ) : null )
                .serviceEndTime ( jobCard.getServiceEndTime ( ) != null ? jobCard.getServiceEndTime ( ) : null )
                .workOrderId ( jobCard.getWorkOrder ( ) != null ? jobCard.getWorkOrder ( ).getId ( ) : null )
                .id ( jobCard.getId ( ) )
                .technicianId ( jobCard.getTechnician ( ) != null ? jobCard.getTechnician ( ).getId ( ) : null )
                .status ( jobCard.getStatus ( ).name ( ) )
                .createdAt ( jobCard.getCreatedAt ( ) )
                .completedAt ( jobCard.getCompletedAt ( ) )
                .garageId ( jobCard.getGarage ( ) != null ? jobCard.getGarage ( ).getId ( ) : null )
                .appointmentId ( jobCard.getAppointment ( ) != null ? jobCard.getAppointment ( ).getId ( ) : null )
                .appointmentService ( jobCard.getService ( ) != null ? appointmentServicesMapper.entityToResponse ( jobCard.getService ( ) ) : null )
                .comments ( jobCard.getComments ( ) )
                .build ( );

        // Set status timestamps
        response.setStatusTimestamps ( jobCard.getStatusTimestamps ( ) );

        // Generate status timeline
        response.setStatusTimeline ( generateStatusTimeline ( jobCard.getStatusTimestamps ( ) ) );

        return response;
    }
  public TechnicianWorkHistoryResponse entityToTechnicianWorkHistoryResponse(JobCard jobCard){
        return TechnicianWorkHistoryResponse.builder()
                .workOrderId(jobCard.getWorkOrder().getId())
                .createdAt(jobCard.getCreatedAt())
                .completedAt(jobCard.getCompletedAt())
                .garageName(jobCard.getGarage().getName())
                .garageLocation(jobCard.getGarage().getGarageLocation().toString())
                .serviceName(jobCard.getService().getService().getAvailableService().getName())
                .build();

    }

    private List<JobCardStatusTimelineEntry> generateStatusTimeline(Map<JobCardStatus, LocalDateTime> statusTimestamps) {
        if ( statusTimestamps == null || statusTimestamps.isEmpty ( ) ) {
            return new ArrayList<> ( );
        }

        List<JobCardStatusTimelineEntry> timeline = new ArrayList<> ( );

        // Convert map to sorted list by timestamp
        statusTimestamps.entrySet ( ).stream ( )
                .sorted ( Map.Entry.comparingByValue ( ) )
                .forEach ( entry -> {
                    JobCardStatusTimelineEntry timelineEntry = new JobCardStatusTimelineEntry ( );
                    timelineEntry.setStatus ( entry.getKey ( ) );
                    timelineEntry.setTimestamp ( entry.getValue ( ) );
                    timelineEntry.setDescription ( getStatusDescription ( entry.getKey ( ) ) );
                    timeline.add ( timelineEntry );
                } );

        // Calculate duration between statuses
        for (int i = 1; i < timeline.size ( ); i++) {
            JobCardStatusTimelineEntry current = timeline.get ( i );
            JobCardStatusTimelineEntry previous = timeline.get ( i - 1 );
            long durationInMinutes = ChronoUnit.MINUTES.between (
                    previous.getTimestamp ( ),
                    current.getTimestamp ( )
            );
            current.setDurationInMinutes ( (int) durationInMinutes );
        }

        return timeline;
    }

    private String getStatusDescription(JobCardStatus status) {
        switch (status) {
            case CREATED:
                return "Job card has been created";
            case ASSIGNED:
                return "Job card assigned to technician";
            case IN_PROGRESS:
                return "Technician has started working";
            case PAUSED:
                return "Work has been temporarily paused";
            case WAITING_FOR_PARTS:
                return "Waiting for spare parts to continue";
            case WAITING_FOR_APPROVAL:
                return "Waiting for customer approval";
            case WORK_COMPLETED:
                return "Technician has finished the work";
            case QUALITY_CHECK_PENDING:
                return "Quality inspection is pending";
            case QUALITY_CHECK_FAILED:
                return "Quality check failed - rework needed";
            case QUALITY_CHECK_PASSED:
                return "Quality check has passed";
            case COMPLETED:
                return "Job card is fully completed";
            case CANCELLED:
                return "Job card has been cancelled";
            case ON_HOLD:
                return "Job card is on hold";
            default:
                return "Unknown status";
        }
    }
}
