package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.InsuranceClaimDocument;

@Repository
public interface InsuranceClaimDocumentRepository extends JpaRepository<InsuranceClaimDocument, Integer> {
	List<InsuranceClaimDocument> findByClaimId(Integer claimId);
}