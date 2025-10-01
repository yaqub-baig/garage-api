package com.autoadda.apis.garage.util;

public enum JobCardStatus {
    // Initial States
    CREATED,                    // JobCard created from appointment service
    ASSIGNED,                   // Assigned to technician

    // Work States
    IN_PROGRESS,               // Technician started working
    PAUSED,                    // Work temporarily paused
    WAITING_FOR_PARTS,         // Need spare parts to continue
    WAITING_FOR_APPROVAL,      // Need customer approval (additional work/cost)

    // Completion States
    WORK_COMPLETED,            // Technician finished the work
    QUALITY_CHECK_PENDING,     // Waiting for quality inspection
    QUALITY_CHECK_FAILED,      // Quality check failed, rework needed
    QUALITY_CHECK_PASSED,      // Quality approved

    // Final States
    COMPLETED,                 // JobCard fully completed
    CANCELLED,                 // JobCard cancelled
    ON_HOLD                    // JobCard on hold
}
