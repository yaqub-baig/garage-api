package com.service.garage.apis.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.service.garage.apis.entity.InsuranceClaim;
import com.service.garage.apis.entity.InsuranceClaimDocument;
import com.service.garage.apis.repository.InsuranceClaimDocumentRepository;
import com.service.garage.apis.repository.InsuranceClaimRepository;
import com.service.garage.apis.util.Status;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InsuranceClaimService {

	@Autowired
	private InsuranceClaimRepository claimRepo;

	@Autowired
	private InsuranceClaimDocumentRepository docRepo;

	/**
	 * Submits a new insurance claim.
	 * 
	 * @param claim The insurance claim to be submitted.
	 * @return The saved insurance claim with status set to SUBMITTED.
	 */
	@Transactional
	public InsuranceClaim submitClaim(InsuranceClaim claim) {
		claim.setStatus(Status.SUBMITTED);
		claim.setRequestDate(LocalDateTime.now());
		return claimRepo.save(claim);
	}

	/**
	 * Updates the status of an existing insurance claim.
	 * 
	 * @param claimId        The ID of the claim to be updated.
	 * @param status         The new status to set for the claim.
	 * @param remarks        Any remarks related to the status update.
	 * @param approvedAmount The amount approved for the claim, if applicable.
	 * @return The updated insurance claim.
	 */
	@Transactional
	public InsuranceClaim updateClaimStatus(Integer claimId, Status status, String remarks, BigDecimal approvedAmount) {
		InsuranceClaim claim = claimRepo.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found"));

		claim.setStatus(status);
		claim.setApprovalRemarks(remarks);
		claim.setApprovedAmount(approvedAmount);
		claim.setUpdatedAt(LocalDateTime.now());

		return claimRepo.save(claim);
	}

	/**
	 * Uploads a document related to an insurance claim.
	 * 
	 * @param claimId  The ID of the claim to which the document belongs.
	 * @param fileName The name of the file being uploaded.
	 * @param fileUrl  The URL where the file is stored.
	 * @param docType  The type of document being uploaded (e.g., INVOICE,
	 *                 ESTIMATE).
	 * @return The saved insurance claim document.
	 */
	@Transactional
	public InsuranceClaimDocument uploadDocument(Integer claimId, String fileName, String fileUrl, String docType) {
		InsuranceClaimDocument doc = new InsuranceClaimDocument();
		doc.setClaimId(claimId);
		doc.setFileName(fileName);
		doc.setFileUrl(fileUrl);
		doc.setDocumentType(docType);
		return docRepo.save(doc);
	}

	/**
	 * Retrieves all documents associated with a specific insurance claim.
	 * 
	 * @param claimId The ID of the claim for which documents are requested.
	 * @return A list of documents related to the specified claim.
	 */
	@Transactional
	public List<InsuranceClaimDocument> getDocuments(Integer claimId) {
		return docRepo.findByClaimId(claimId);
	}

	/**
	 * Retrieves all insurance claims for a specific vehicle owner.
	 * 
	 * @param ownerId The ID of the vehicle owner.
	 * @return A list of insurance claims associated with the specified owner.
	 */
	@Transactional
	public List<InsuranceClaim> getClaimsForOwner(Integer ownerId) {
		return claimRepo.findByVehicleOwnerId(ownerId);
	}

	/**
	 * Retrieves all insurance claims for a specific garage.
	 * 
	 * @param garageId The ID of the garage.
	 * @return A list of insurance claims associated with the specified garage.
	 */
	@Transactional
	public List<InsuranceClaim> getClaimsForGarage(Integer garageId) {
		return claimRepo.findByGarageId(garageId);
	}

	/**
	 * Retrieves an insurance claim by its ID.
	 * 
	 * @param claimId The ID of the claim to retrieve.
	 * @return The insurance claim with the specified ID.
	 */
	@Transactional
	public InsuranceClaim getClaimById(Integer claimId) {
		return claimRepo.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found"));
	}

	/**
	 * Retrieves all insurance claims.
	 * 
	 * @return A list of all insurance claims.
	 */
	@Transactional
	public List<InsuranceClaim> getAllClaims() {
		return claimRepo.findAll();
	}

	/**
	 * Deletes an insurance claim by its ID.
	 * 
	 * @param claimId The ID of the claim to delete.
	 */
	@Transactional
	public void deleteClaim(Integer claimId) {
		InsuranceClaim claim = claimRepo.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found"));
		claimRepo.delete(claim);
	}
}
