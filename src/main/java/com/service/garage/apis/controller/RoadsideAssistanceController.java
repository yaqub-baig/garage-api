package com.service.garage.apis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.entity.RoadsideAssistance;
import com.service.garage.apis.service.RoadsideAssistanceService;
import com.service.garage.apis.util.Status;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/roadside-assistance")
public class RoadsideAssistanceController {

	@Autowired
	private RoadsideAssistanceService assistanceService;

	@PostMapping
	public ResponseEntity<RoadsideAssistance> create(@RequestBody RoadsideAssistance request) {
		return ResponseEntity.ok(assistanceService.createRequest(request));
	}

	@GetMapping("/owner/{ownerId}")
	public ResponseEntity<List<RoadsideAssistance>> getByOwner(@PathVariable Integer ownerId) {
		return ResponseEntity.ok(assistanceService.getRequestsByOwner(ownerId));
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<RoadsideAssistance> updateStatus(@PathVariable Integer id, @RequestParam Status status,
			@RequestParam(required = false) String remarks) {
		return ResponseEntity.ok(assistanceService.updateStatus(id, status, remarks));
	}

	@GetMapping("/{id}")
	public ResponseEntity<RoadsideAssistance> getById(@PathVariable Integer id) {
		return assistanceService.getRequestById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}
}
