package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.InsuranceClaimDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceClaimDocumentRepository extends JpaRepository<InsuranceClaimDocument, Integer> {
    List<InsuranceClaimDocument> findByClaimId(Integer claimId);
}