package com.service.garage.apis.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.entity.JobCard;
import com.service.garage.apis.exception.MemberNotFoundException;
import com.service.garage.apis.request.AssignServiceRequest;
import com.service.garage.apis.request.JobCardStatusRequest;
import com.service.garage.apis.response.GenericResponse;
import com.service.garage.apis.service.JobCardService;
import com.service.garage.apis.util.Status;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/jobcards")
@RequiredArgsConstructor
public class JobCardController {

	private final JobCardService jobCardService;

	@PostMapping("/assign")
	public ResponseEntity<GenericResponse> assignTechnician(@RequestBody AssignServiceRequest request) {
		JobCard jobCard = jobCardService.assignTechnicianToService(request);
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
