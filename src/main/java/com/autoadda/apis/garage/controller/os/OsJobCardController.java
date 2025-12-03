package com.autoadda.apis.garage.controller.os;

import com.autoadda.apis.garage.entity.os.OsJobCard;
import com.autoadda.apis.garage.exception.MemberNotFoundException;
import com.autoadda.apis.garage.request.os.OsAssignServiceRequest;
import com.autoadda.apis.garage.request.os.OsJobCardStatusRequest;
import com.autoadda.apis.garage.response.os.OsGenericResponse;
import com.autoadda.apis.garage.response.os.OsJobCardResponse;
import com.autoadda.apis.garage.service.os.OsJobCardService;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/os-jobcards")
@RequiredArgsConstructor
public class OsJobCardController {

    private final OsJobCardService jobCardService;

    @PostMapping("/assign")
    public ResponseEntity<OsGenericResponse> assignTechnician(@RequestBody OsAssignServiceRequest request) {
        OsJobCardResponse jobCard = jobCardService.assignTechnicianToService(request);
        return ResponseEntity.ok(OsGenericResponse.makeSuccessResponse("Technician assigned", jobCard));
    }

    @PutMapping("/status")
    public ResponseEntity<OsGenericResponse> updateJobCardStatus(@RequestBody OsJobCardStatusRequest request) {
        OsJobCard updated = jobCardService.updateJobCardStatus(request);
        return ResponseEntity.ok(OsGenericResponse.makeSuccessResponse("Jobcard status updated", updated));
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<OsGenericResponse> getTechnicianJobCards(@PathVariable Integer technicianId,
                                                                 @RequestParam(required = false) Status status) throws MemberNotFoundException {
        List<OsJobCard> jobCards = jobCardService.getTechnicianJobCards(technicianId, status);
        return ResponseEntity.ok(OsGenericResponse.makeSuccessResponse(jobCards));
    }

    @GetMapping("/other-service/{otherServiceId}")
    public ResponseEntity<OsGenericResponse> getOtherServiceJobCards(@PathVariable Integer otherServiceId,
                                                             @RequestParam(required = false) Status status) {
        List<OsJobCard> jobCards = jobCardService.getGarageJobCards(otherServiceId, status);
        return ResponseEntity.ok(OsGenericResponse.makeSuccessResponse(jobCards));
    }
}
