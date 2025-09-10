package com.autoadda.apis.garage.controller;

import java.util.List;

import com.autoadda.apis.garage.response.JobCardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.exception.MemberNotFoundException;
import com.autoadda.apis.garage.request.AssignServiceRequest;
import com.autoadda.apis.garage.request.JobCardStatusRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.JobCardService;
import com.autoadda.apis.garage.util.Status;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobcards")
@RequiredArgsConstructor
public class JobCardController {

	private final JobCardService jobCardService;

	@PostMapping("/assign")
	public ResponseEntity<GenericResponse> assignTechnician(@RequestBody AssignServiceRequest request) {
		JobCardResponse jobCard = jobCardService.assignTechnicianToService(request);
		return ResponseEntity.ok(GenericResponse.success("Technician assigned", jobCard));
	}

	@PutMapping("/status")
	public ResponseEntity<GenericResponse> updateJobCardStatus(@RequestBody JobCardStatusRequest request) {
		JobCard updated = jobCardService.updateJobCardStatus(request);
		return ResponseEntity.ok(GenericResponse.success("Jobcard status updated", updated));
	}

	@GetMapping("/technician/{technicianId}")
	public ResponseEntity<GenericResponse> getTechnicianJobCards(@PathVariable Integer technicianId,
			@RequestParam(required = false) Status status) throws MemberNotFoundException {
		List<JobCard> jobCards = jobCardService.getTechnicianJobCards(technicianId, status);
		return ResponseEntity.ok(GenericResponse.success(jobCards));
	}

	@GetMapping("/garage/{garageId}")
	public ResponseEntity<GenericResponse> getGarageJobCards(@PathVariable Integer garageId,
			@RequestParam(required = false) Status status) {
		List<JobCard> jobCards = jobCardService.getGarageJobCards(garageId, status);
		return ResponseEntity.ok(GenericResponse.success(jobCards));
	}
}
