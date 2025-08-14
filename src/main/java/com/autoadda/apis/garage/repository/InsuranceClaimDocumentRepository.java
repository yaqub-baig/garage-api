package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.InsuranceClaimDocument;

@Repository
public interface InsuranceClaimDocumentRepository extends JpaRepository<InsuranceClaimDocument, Integer> {
	List<InsuranceClaimDocument> findByClaimId(Integer claimId);
}