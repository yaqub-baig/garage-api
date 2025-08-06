package com.service.garage.apis.garage.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.service.garage.apis.entity.InsuranceClaim;
import com.service.garage.apis.entity.InsuranceClaimDocument;
import com.service.garage.apis.repository.InsuranceClaimDocumentRepository;
import com.service.garage.apis.repository.InsuranceClaimRepository;
import com.service.garage.apis.service.InsuranceClaimService;
import com.service.garage.apis.util.Status;

@ExtendWith(MockitoExtension.class)
class InsuranceClaimServiceTest {

    @InjectMocks
    private InsuranceClaimService insuranceClaimService;

    @Mock
    private InsuranceClaimRepository claimRepo;

    @Mock
    private InsuranceClaimDocumentRepository docRepo;

    private InsuranceClaim mockClaim;
    private InsuranceClaimDocument mockDoc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockClaim = new InsuranceClaim();
        mockClaim.setId(1);
        mockClaim.setVehicleOwnerId(101);
        mockClaim.setGarageId(501);
        mockClaim.setStatus(Status.PENDING);
        mockClaim.setRequestDate(LocalDateTime.now());

        mockDoc = new InsuranceClaimDocument();
        mockDoc.setId(1);
        mockDoc.setClaimId(1);
        mockDoc.setFileName("report.pdf");
        mockDoc.setFileUrl("http://url.com/report.pdf");
        mockDoc.setDocumentType("REPORT");
    }

    @Test
    void testSubmitClaim() {
        when(claimRepo.save(any(InsuranceClaim.class))).thenReturn(mockClaim);

        InsuranceClaim saved = insuranceClaimService.submitClaim(mockClaim);
        assertEquals(Status.SUBMITTED, saved.getStatus());
        verify(claimRepo).save(mockClaim);
    }

    @Test
    void testUpdateClaimStatus() {
        when(claimRepo.findById(1)).thenReturn(Optional.of(mockClaim));
        when(claimRepo.save(any())).thenReturn(mockClaim);

        InsuranceClaim updated = insuranceClaimService.updateClaimStatus(
                1, Status.APPROVED, "Approved", BigDecimal.valueOf(1500));

        assertEquals(Status.APPROVED, updated.getStatus());
        assertEquals("Approved", updated.getApprovalRemarks());
        assertEquals(BigDecimal.valueOf(1500), updated.getApprovedAmount());
        verify(claimRepo).save(mockClaim);
    }

    @Test
    void testUploadDocument() {
        when(docRepo.save(any())).thenReturn(mockDoc);

        InsuranceClaimDocument savedDoc = insuranceClaimService.uploadDocument(1, "report.pdf", "url", "REPORT");
        assertEquals("report.pdf", savedDoc.getFileName());
        verify(docRepo).save(any());
    }

    @Test
    void testGetDocuments() {
        when(docRepo.findByClaimId(1)).thenReturn(List.of(mockDoc));

        List<InsuranceClaimDocument> docs = insuranceClaimService.getDocuments(1);
        assertEquals(1, docs.size());
        verify(docRepo).findByClaimId(1);
    }

    @Test
    void testGetClaimsForOwner() {
        when(claimRepo.findByVehicleOwnerId(101)).thenReturn(List.of(mockClaim));

        List<InsuranceClaim> claims = insuranceClaimService.getClaimsForOwner(101);
        assertEquals(1, claims.size());
        verify(claimRepo).findByVehicleOwnerId(101);
    }

    @Test
    void testGetClaimsForGarage() {
        when(claimRepo.findByGarageId(501)).thenReturn(List.of(mockClaim));

        List<InsuranceClaim> claims = insuranceClaimService.getClaimsForGarage(501);
        assertEquals(1, claims.size());
        verify(claimRepo).findByGarageId(501);
    }

    @Test
    void testGetClaimById() {
        when(claimRepo.findById(1)).thenReturn(Optional.of(mockClaim));

        InsuranceClaim claim = insuranceClaimService.getClaimById(1);
        assertEquals(1, claim.getId());
        verify(claimRepo).findById(1);
    }

    @Test
    void testGetAllClaims() {
        when(claimRepo.findAll()).thenReturn(List.of(mockClaim));

        List<InsuranceClaim> claims = insuranceClaimService.getAllClaims();
        assertEquals(1, claims.size());
        verify(claimRepo).findAll();
    }

    @Test
    void testDeleteClaim() {
        when(claimRepo.findById(1)).thenReturn(Optional.of(mockClaim));

        insuranceClaimService.deleteClaim(1);
        verify(claimRepo).delete(mockClaim);
    }

    @Test
    void testUpdateClaimStatusThrowsException() {
        when(claimRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                insuranceClaimService.updateClaimStatus(999, Status.REJECTED, "Invalid", BigDecimal.ZERO));
    }

    @Test
    void testGetClaimByIdThrowsException() {
        when(claimRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                insuranceClaimService.getClaimById(999));
    }

    @Test
    void testDeleteClaimThrowsException() {
        when(claimRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> insuranceClaimService.deleteClaim(999));
    }
}
