package com.autoadda.apis.garage.service;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.JobCardAdditionalRequest;
import com.autoadda.apis.garage.repository.JobCardAdditionalRequestRepository;
import com.autoadda.apis.garage.request.JobCardAdditionalAPIRequest;
import com.autoadda.apis.garage.util.DateHelper;
import com.autoadda.apis.garage.util.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdditionalRequestService {

    private final JobCardAdditionalRequestRepository additionalRequestRepo;

    @Transactional
    public JobCardAdditionalRequest createAdditionalRequest(JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest additionalRequest = JobCardAdditionalRequest.builder()
                .garageId(request.getGarageId())
                .customerId(request.getCustomerId())
                .technicianId(request.getTechnicianId())
                .status(Status.REQUESTED)
                .requestType(request.getRequestType())
                .comments(request.getComments())
                .insert_tms(DateHelper.dbDate())
                .build();

        return additionalRequestRepo.save(additionalRequest);
    }

    @Transactional
    public JobCardAdditionalRequest updateAdditionalRequest(JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest existing = additionalRequestRepo.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("Additional request not found"));

        existing.setStatus(request.getStatus());
        return additionalRequestRepo.save(existing);
    }
}
