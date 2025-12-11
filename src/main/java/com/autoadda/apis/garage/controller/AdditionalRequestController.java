package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.entity.JobCardAdditionalRequest;
import com.autoadda.apis.garage.request.JobCardAdditionalAPIRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.AdditionalRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/additional-requests")
@RequiredArgsConstructor
public class AdditionalRequestController {

    private final AdditionalRequestService additionalRequestService;

    @PostMapping
    public ResponseEntity<GenericResponse> createRequest(@RequestBody JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest result = additionalRequestService.createAdditionalRequest(request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Additional request created", result));
    }


    @PutMapping
    public ResponseEntity<GenericResponse> updateRequest(@RequestBody JobCardAdditionalAPIRequest request) {
        JobCardAdditionalRequest result = additionalRequestService.updateAdditionalRequest(request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Additional request updated", result));
    }
}
