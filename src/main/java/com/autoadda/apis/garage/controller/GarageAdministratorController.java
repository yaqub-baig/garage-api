package com.autoadda.apis.garage.controller;

import com.autoadda.apis.garage.request.ApproveGarageRequest;
import com.autoadda.apis.garage.request.GarageImportRequest;
import com.autoadda.apis.garage.request.RejectGarageRequest;
import com.autoadda.apis.garage.response.GarageImportResult;
import com.autoadda.apis.garage.response.GarageResponse;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.service.GarageManagementService;
import com.autoadda.apis.garage.util.GarageVerificationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrator controller for garage verification and management
 * All endpoints in this controller are admin-only operations
 */
@RestController
@RequestMapping("/admin/garages")
@RequiredArgsConstructor
@Slf4j
public class GarageAdministratorController {

    private final GarageManagementService garageManagementService;

    /**
     * Bulk import garages scraped from Google Maps (Apify)
     * POST /admin/garages/import
     */
    @PostMapping("/import")
    public ResponseEntity<GenericResponse> importScrapedGarages(
            @RequestBody List<GarageImportRequest> records) {
        log.info("Importing {} scraped garage records", records.size());
        GarageImportResult result = garageManagementService.importScrapedGarages(records);
        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Import complete", result));
    }

    /**
     * Approve a pending garage
     * POST /admin/garages/{garageId}/approve
     */
    @PostMapping("/{garageId}/approve")
    public ResponseEntity<GenericResponse> approveGarage(
            @PathVariable Integer garageId,
            @RequestBody ApproveGarageRequest request,
            @RequestHeader("X-Auto-Adda-Id") String adminId) {
        log.info("Admin {} approving garage {}", adminId, garageId);

        GarageResponse response = garageManagementService.approveGarage(
                garageId,
                Integer.parseInt(adminId)
        );

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garage approved successfully", response));
    }

    /**
     * Reject a pending garage with reason
     * POST /admin/garages/{garageId}/reject
     */
    @PostMapping("/{garageId}/reject")
    public ResponseEntity<GenericResponse> rejectGarage(
            @PathVariable Integer garageId,
            @RequestBody RejectGarageRequest request,
            @RequestHeader("X-Auto-Adda-Id") String adminId) {
        log.info("Admin {} rejecting garage {} with reason: {}",
                adminId, garageId, request.getRejectionReason());

        GarageResponse response = garageManagementService.rejectGarage(
                garageId,
                Integer.parseInt(adminId),
                request.getRejectionReason()
        );

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garage rejected successfully", response));
    }

    /**
     * Get all pending garages awaiting approval
     * GET /admin/garages/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<GenericResponse> getPendingGarages() {
        log.info("Fetching all pending garages");
        List<GarageResponse> responses = garageManagementService.getPendingGarages();

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Pending garages retrieved successfully", responses));
    }

    /**
     * Get all rejected garages
     * GET /admin/garages/rejected
     */
    @GetMapping("/rejected")
    public ResponseEntity<GenericResponse> getRejectedGarages() {
        log.info("Fetching all rejected garages");
        List<GarageResponse> responses = garageManagementService.getRejectedGarages();

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Rejected garages retrieved successfully", responses));
    }

    /**
     * Get all approved garages
     * GET /admin/garages/approved
     */
    @GetMapping("/approved")
    public ResponseEntity<GenericResponse> getApprovedGarages() {
        log.info("Fetching all approved garages");
        List<GarageResponse> responses = garageManagementService.getApprovedGarages();

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Approved garages retrieved successfully", responses));
    }

    /**
     * Get garages by verification status
     * GET /admin/garages/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<GenericResponse> getGaragesByStatus(
            @PathVariable GarageVerificationStatus status) {
        log.info("Fetching garages with status: {}", status);
        List<GarageResponse> responses = garageManagementService.getGaragesByVerificationStatus(status);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garages retrieved successfully", responses));
    }

    /**
     * Get verification statistics for admin dashboard
     * GET /admin/garages/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<GenericResponse> getVerificationStatistics() {
        log.info("Fetching garage verification statistics");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGarages", garageManagementService.getTotalGaragesCount());
        stats.put("pendingCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.PENDING));
        stats.put("approvedCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.APPROVED));
        stats.put("rejectedCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.REJECTED));
        stats.put("activeCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.ACTIVE));
        stats.put("suspendedCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.SUSPENDED));
        stats.put("inactiveCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.INACTIVE));
        stats.put("blockedCount", garageManagementService.getGarageCountByStatus(GarageVerificationStatus.BLOCKED));

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Statistics retrieved successfully", stats));
    }

    /**
     * Get garages pending for more than X days
     * GET /admin/garages/pending/stale?days={days}
     */
    @GetMapping("/pending/stale")
    public ResponseEntity<GenericResponse> getStalePendingGarages(
            @RequestParam(defaultValue = "7") Integer days) {
        log.info("Fetching garages pending for more than {} days", days);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        List<GarageResponse> responses = garageManagementService.getPendingGaragesOlderThan(cutoffDate);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Stale pending garages retrieved successfully", responses));
    }

    /**
     * Get garages with multiple resubmissions
     * GET /admin/garages/resubmissions?minCount={count}
     */
    @GetMapping("/resubmissions")
    public ResponseEntity<GenericResponse> getGaragesWithMultipleResubmissions(
            @RequestParam(defaultValue = "2") Integer minCount) {
        log.info("Fetching garages with {} or more resubmissions", minCount);

        List<GarageResponse> responses = garageManagementService.getGaragesWithMultipleResubmissions(minCount);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garages with multiple resubmissions retrieved successfully", responses));
    }

    /**
     * Get garages reviewed by specific admin
     * GET /admin/garages/reviewed-by/{adminId}
     */
    @GetMapping("/reviewed-by/{adminId}")
    public ResponseEntity<GenericResponse> getGaragesReviewedByAdmin(
            @PathVariable Integer adminId) {
        log.info("Fetching garages reviewed by admin {}", adminId);

        List<GarageResponse> responses = garageManagementService.getGaragesReviewedBy(adminId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garages reviewed by admin retrieved successfully", responses));
    }

    /**
     * Get garages approved by specific admin
     * GET /admin/garages/approved-by/{adminId}
     */
    @GetMapping("/approved-by/{adminId}")
    public ResponseEntity<GenericResponse> getGaragesApprovedByAdmin(
            @PathVariable Integer adminId) {
        log.info("Fetching garages approved by admin {}", adminId);

        List<GarageResponse> responses = garageManagementService.getGaragesApprovedBy(adminId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garages approved by admin retrieved successfully", responses));
    }

    /**
     * Get garages rejected by specific admin
     * GET /admin/garages/rejected-by/{adminId}
     */
    @GetMapping("/rejected-by/{adminId}")
    public ResponseEntity<GenericResponse> getGaragesRejectedByAdmin(
            @PathVariable Integer adminId) {
        log.info("Fetching garages rejected by admin {}", adminId);

        List<GarageResponse> responses = garageManagementService.getGaragesRejectedBy(adminId);

        return ResponseEntity.ok(GenericResponse.makeSuccessResponse(
                "Garages rejected by admin retrieved successfully", responses));
    }
}
