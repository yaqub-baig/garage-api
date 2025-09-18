package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.util.JobCardStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class JobCardStatusFlowService {

    // Define valid JobCard status transitions
    private static final Map<JobCardStatus, List<JobCardStatus>> VALID_TRANSITIONS = Map.ofEntries (

            Map.entry ( JobCardStatus.CREATED, Arrays.asList (
                    JobCardStatus.ASSIGNED,
                    JobCardStatus.CANCELLED,
                    JobCardStatus.ON_HOLD
            ) ),

            Map.entry ( JobCardStatus.ASSIGNED, Arrays.asList (
                    JobCardStatus.IN_PROGRESS,
                    JobCardStatus.ON_HOLD,
                    JobCardStatus.CANCELLED
            ) ),

            Map.entry ( JobCardStatus.IN_PROGRESS, Arrays.asList (
                    JobCardStatus.PAUSED,
                    JobCardStatus.WAITING_FOR_PARTS,
                    JobCardStatus.WAITING_FOR_APPROVAL,
                    JobCardStatus.WORK_COMPLETED,
                    JobCardStatus.ON_HOLD,
                    JobCardStatus.CANCELLED
            ) ),

            Map.entry ( JobCardStatus.PAUSED, Arrays.asList (
                    JobCardStatus.IN_PROGRESS,
                    JobCardStatus.WAITING_FOR_PARTS,
                    JobCardStatus.ON_HOLD,
                    JobCardStatus.CANCELLED
            ) ),

            Map.entry ( JobCardStatus.WAITING_FOR_PARTS, Arrays.asList (
                    JobCardStatus.IN_PROGRESS,
                    JobCardStatus.ON_HOLD,
                    JobCardStatus.CANCELLED
            ) ),

            Map.entry ( JobCardStatus.WAITING_FOR_APPROVAL, Arrays.asList (
                    JobCardStatus.IN_PROGRESS,
                    JobCardStatus.CANCELLED,
                    JobCardStatus.ON_HOLD
            ) ),

            Map.entry ( JobCardStatus.WORK_COMPLETED, Arrays.asList (
                    JobCardStatus.QUALITY_CHECK_PENDING,
                    JobCardStatus.COMPLETED // Skip quality check if not required
            ) ),

            Map.entry ( JobCardStatus.QUALITY_CHECK_PENDING, Arrays.asList (
                    JobCardStatus.QUALITY_CHECK_PASSED,
                    JobCardStatus.QUALITY_CHECK_FAILED
            ) ),

            Map.entry ( JobCardStatus.QUALITY_CHECK_FAILED, Arrays.asList (
                    JobCardStatus.IN_PROGRESS, // Rework
                    JobCardStatus.CANCELLED
            ) ),

            Map.entry ( JobCardStatus.QUALITY_CHECK_PASSED, Arrays.asList (
                    JobCardStatus.COMPLETED
            ) ),

            Map.entry ( JobCardStatus.ON_HOLD, Arrays.asList (
                    JobCardStatus.IN_PROGRESS,
                    JobCardStatus.CANCELLED
            ) )
    );

    public boolean isValidTransition(JobCardStatus currentStatus, JobCardStatus newStatus) {
        List<JobCardStatus> validNextStates = VALID_TRANSITIONS.get ( currentStatus );
        return validNextStates != null && validNextStates.contains ( newStatus );
    }

    public List<JobCardStatus> getValidNextStates(JobCardStatus currentStatus) {
        return VALID_TRANSITIONS.getOrDefault ( currentStatus, Arrays.asList ( ) );
    }

    public boolean isTerminalStatus(JobCardStatus status) {
        return status == JobCardStatus.COMPLETED || status == JobCardStatus.CANCELLED;
    }

    public boolean isActiveWorkStatus(JobCardStatus status) {
        return status == JobCardStatus.IN_PROGRESS ||
                status == JobCardStatus.PAUSED ||
                status == JobCardStatus.WAITING_FOR_PARTS ||
                status == JobCardStatus.WAITING_FOR_APPROVAL;
    }
}