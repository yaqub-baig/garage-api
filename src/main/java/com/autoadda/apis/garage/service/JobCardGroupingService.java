package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.exception.MemberNotFoundException;
import com.autoadda.apis.garage.response.GroupedJobCardResponse;
import com.autoadda.apis.garage.util.JobCardStatus;

/**
 * Service for grouping job cards by work order.
 * This service provides read-only operations for organizing job cards
 * into grouped responses based on work order relationships.
 */
public interface JobCardGroupingService {

    /**
     * Retrieves job cards grouped by work order ID for a specific garage.
     *
     * @param garageId the ID of the garage
     * @param status   the status of the job cards to filter by (optional)
     * @return GroupedJobCardResponse with job cards organized by work order
     */
    GroupedJobCardResponse getGroupedJobCardsByWorkOrder(Integer garageId, JobCardStatus status);

    /**
     * Retrieves job cards grouped by work order ID for a specific technician.
     *
     * @param technicianId the ID of the technician
     * @param status       the status of the job cards to filter by (optional)
     * @return GroupedJobCardResponse with job cards organized by work order
     * @throws MemberNotFoundException if technician is not found
     */
    GroupedJobCardResponse getGroupedTechnicianJobCardsByWorkOrder(Integer technicianId, JobCardStatus status)
            throws MemberNotFoundException;
}
