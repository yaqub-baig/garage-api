package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.exception.MemberNotFoundException;
import com.autoadda.apis.garage.request.AssignServiceRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.GroupedJobCardResponse;
import com.autoadda.apis.garage.response.JobCardResponse;
import com.autoadda.apis.garage.response.JobCardStatusTimelineEntry;
import com.autoadda.apis.garage.service.JobCardGroupingService;
import com.autoadda.apis.garage.service.JobCardService;
import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/jobcards")
@RequiredArgsConstructor
public class JobCardController {

    private final JobCardService jobCardService;
    private final JobCardGroupingService jobCardGroupingService;

    @PostMapping("/assign")
    public ResponseEntity<GenericResponse> assignTechnician(@RequestBody AssignServiceRequest request) {
        JobCardResponse jobCard = jobCardService.assignTechnicianToService(request);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Technician assigned", jobCard));
    }

    @PutMapping("/status/{jobCardId}")
    public ResponseEntity<GenericResponse> updateJobCardStatus(@PathVariable Integer jobCardId, @RequestParam JobCardStatus status) {
        JobCardResponse updated = jobCardService.updateJobCardStatus(jobCardId, status);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Jobcard status updated", updated));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<GenericResponse> getTechnicianJobCards(@PathVariable Integer technicianId,
                                                                 @RequestParam(required = false) JobCardStatus status) throws MemberNotFoundException {
        List<JobCardResponse> jobCards = jobCardService.getTechnicianJobCards(technicianId, status);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(jobCards));
    }

    @GetMapping("/garage/{garageId}")
    public ResponseEntity<GenericResponse> getGarageJobCards(@PathVariable Integer garageId,
                                                             @RequestParam(required = false) JobCardStatus status) {
        List<JobCardResponse> jobCards = jobCardService.getGarageJobCards(garageId, status);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(jobCards));
    }

    @GetMapping("/{jobCardId}")
    public ResponseEntity<JobCardResponse> getJobCardById(@PathVariable Integer jobCardId) {
        JobCardResponse jobCard = jobCardService.getJobCardById(jobCardId);
        return ResponseEntity.ok(jobCard);
    }

    @GetMapping("/{jobCardId}/timeline")
    public ResponseEntity<List<JobCardStatusTimelineEntry>> getJobCardTimeline(@PathVariable Integer jobCardId) {
        JobCardResponse jobCard = jobCardService.getJobCardById(jobCardId);
        return ResponseEntity.ok(jobCard.getStatusTimeline());
    }

    @GetMapping("/garage/{garageId}/grouped")
    public ResponseEntity<GenericResponse> getGroupedGarageJobCards(@PathVariable Integer garageId,
                                                                    @RequestParam(required = false) JobCardStatus status) {
        GroupedJobCardResponse groupedJobCards = jobCardGroupingService.getGroupedJobCardsByWorkOrder(garageId, status);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Job cards grouped by work order", groupedJobCards));
    }

    @GetMapping("/technician/{technicianId}/grouped")
    public ResponseEntity<GenericResponse> getGroupedTechnicianJobCards(@PathVariable Integer technicianId,
                                                                        @RequestParam(required = false) JobCardStatus status) throws MemberNotFoundException {
        GroupedJobCardResponse groupedJobCards = jobCardGroupingService.getGroupedTechnicianJobCardsByWorkOrder(technicianId, status);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse("Technician job cards grouped by work order", groupedJobCards));
    }
}
