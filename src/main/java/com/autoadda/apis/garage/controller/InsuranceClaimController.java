package com.autoadda.apis.garage.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autoadda.apis.garage.entity.InsuranceClaim;
import com.autoadda.apis.garage.entity.InsuranceClaimDocument;
import com.autoadda.apis.garage.service.InsuranceClaimService;
import com.autoadda.apis.garage.util.Status;



@RestController
@RequestMapping("/api/insurance-claims")
public class InsuranceClaimController {

	@Autowired
	private InsuranceClaimService claimService;

	@PostMapping
	public ResponseEntity<InsuranceClaim> submitClaim(@RequestBody InsuranceClaim claim) {
		return ResponseEntity.ok(claimService.submitClaim(claim));
	}

	@PutMapping("/{claimId}/status")
	public ResponseEntity<InsuranceClaim> updateStatus(@PathVariable Integer claimId, @RequestParam Status status,
			@RequestParam(required = false) String remarks, @RequestParam(required = false) BigDecimal approvedAmount) {
		return ResponseEntity.ok(claimService.updateClaimStatus(claimId, status, remarks, approvedAmount));
	}

	@PostMapping("/{claimId}/documents")
	public ResponseEntity<InsuranceClaimDocument> uploadDoc(@PathVariable Integer claimId,
			@RequestParam String fileName, @RequestParam String fileUrl, @RequestParam String docType) {
		return ResponseEntity.ok(claimService.uploadDocument(claimId, fileName, fileUrl, docType));
	}

	@GetMapping("/{claimId}/documents")
	public ResponseEntity<List<InsuranceClaimDocument>> getDocs(@PathVariable Integer claimId) {
		return ResponseEntity.ok(claimService.getDocuments(claimId));
	}

	@GetMapping("/owner/{ownerId}")
	public ResponseEntity<List<InsuranceClaim>> getByOwner(@PathVariable Integer ownerId) {
		return ResponseEntity.ok(claimService.getClaimsForOwner(ownerId));
	}

	@GetMapping("/garage/{garageId}")
	public ResponseEntity<List<InsuranceClaim>> getByGarage(@PathVariable Integer garageId) {
		return ResponseEntity.ok(claimService.getClaimsForGarage(garageId));
	}
	@GetMapping("/{claimId}")
	public ResponseEntity<InsuranceClaim> getClaimById(@PathVariable Integer claimId) {
		return ResponseEntity.ok(claimService.getClaimById(claimId));
	}
	@DeleteMapping("/{claimId}")
	public ResponseEntity<Void> deleteClaim(@PathVariable Integer claimId) {
		claimService.deleteClaim(claimId);
		return ResponseEntity.noContent().build();
	}
}
