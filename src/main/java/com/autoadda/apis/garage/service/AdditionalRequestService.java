package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.JobCardAdditionalRequest;
import com.autoadda.apis.garage.repository.JobCardAdditionalRequestRepository;
import com.autoadda.apis.garage.request.JobCardAdditionalAPIRequest;
import com.autoadda.apis.garage.util.DateHelper;
import com.autoadda.apis.garage.util.Status;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdditionalRequestService {

    private final JobCardAdditionalRequestRepository additionalRequestRepo;

    /**
     * Creates a new additional request for a job card.
     *
     * @param request the API request containing details for the additional request
     * @return the created JobCardAdditionalRequest entity
     */
    @Transactional
    public JobCardAdditionalRequest createAdditionalRequest(JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest additionalRequest = JobCardAdditionalRequest.builder ( ).garageId ( request.getGarageId ( ) )
                .customerId ( request.getCustomerId ( ) ).technicianId ( request.getTechnicianId ( ) ).status ( Status.REQUESTED )
                .requestType ( request.getRequestType ( ) ).comments ( request.getComments ( ) ).insert_tms ( DateHelper.dbDate ( ) )
                .build ( );

        return additionalRequestRepo.save ( additionalRequest );
    }

    /**
     * Updates the status of an existing additional request.
     *
     * @param request the API request containing the request ID and new status
     * @return the updated JobCardAdditionalRequest entity
     */
    @Transactional
    public JobCardAdditionalRequest updateAdditionalRequest(JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest existing = additionalRequestRepo.findById ( request.getRequestId ( ) )
                .orElseThrow ( () -> new RuntimeException ( "Additional request not found" ) );

        existing.setStatus ( request.getStatus ( ) );
        return additionalRequestRepo.save ( existing );
    }

    /**
     * Deletes an additional request by its ID.
     *
     * @param requestId the ID of the additional request to delete
     * @return true if the request was deleted, false if it did not exist
     * @return true if the request was deleted, false if it did not exist
     */
    @Transactional
    public boolean deleteAdditionalRequest(Integer requestId) {
        if ( additionalRequestRepo.existsById ( requestId ) ) {
            additionalRequestRepo.deleteById ( requestId );
            return true;
        }
        return false;
    }

    /**
     * Retrieves an additional request by its ID.
     *
     * @param requestId the ID of the additional request to retrieve
     * @return the JobCardAdditionalRequest entity if found, or null if not found
     */
    @Transactional
    public JobCardAdditionalRequest getAdditionalRequestById(Integer requestId) {
        return additionalRequestRepo.findById ( requestId )
                .orElseThrow ( () -> new RuntimeException ( "Additional request not found" ) );
    }
}
