package com.autoadda.apis.garage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autoadda.apis.garage.entity.JobCardAdditionalRequest;
import com.autoadda.apis.garage.request.JobCardAdditionalAPIRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.AdditionalRequestService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/additional-requests")
@RequiredArgsConstructor
public class AdditionalRequestController {

    private final AdditionalRequestService additionalRequestService;

    @PostMapping
    public ResponseEntity<GenericResponse> createRequest(@RequestBody JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest result = additionalRequestService.createAdditionalRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Additional request created", result));
    }


    @PutMapping
    public ResponseEntity<GenericResponse> updateRequest(@RequestBody JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest result = additionalRequestService.updateAdditionalRequest(request);
        return ResponseEntity.ok(GenericResponse.success("Additional request updated", result));
    }
}
