package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.RequestingGarageAndTechnician;
import com.autoadda.apis.garage.entity.Technician;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.BadRequestException;
import com.autoadda.apis.garage.exception.ConflictException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.RequestingGarageTechnicianMapper;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.RequestingGarageTechnicianRepository;
import com.autoadda.apis.garage.repository.TechnicianRepository;
import com.autoadda.apis.garage.request.NotificationContent;
import com.autoadda.apis.garage.request.RequestingGarageTechnicianRequest;
import com.autoadda.apis.garage.response.NotificationResult;
import com.autoadda.apis.garage.response.RequestingGarageTechnicianResponse;
import com.autoadda.apis.garage.util.ApprovalStatus;
import com.autoadda.apis.garage.util.NotificationType;
import com.autoadda.apis.garage.util.SendBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestingGarageTechnicianService {

    // Constants for error messages
    private static final String GARAGE_NOT_FOUND = "Garage not found with id: ";
    private static final String TECHNICIAN_NOT_FOUND = "Technician not found with id: ";
    private static final String REQUEST_NOT_FOUND = "Request not found with id: ";
    private static final String ALREADY_LINKED = "Garage and technician are already linked";
    private static final String DUPLICATE_REQUEST = "A pending request already exists between this garage and technician";
    private static final String INVALID_STATUS = "Request is not in HOLD status and cannot be processed";
    private static final String UNAUTHORIZED_ACTION = "You are not authorized to approve/reject this request";
    public static final String GARAGE = "Garage";
    public static final String GARAGE_OWNER = "GARAGE_OWNER";
    public static final String TECHNICIAN = "Technician";
    public static final String GARAGE_TECH = "GARAGE_TECH";

    private final RequestingGarageTechnicianRepository requestRepository;
    private final GarageRepository garageRepository;
    private final TechnicianRepository technicianRepository;
    private final RequestingGarageTechnicianMapper mapper;
    private final NotificationDispatchService notificationDispatchService;
    private final NotificationMessageBuilder notificationMessageBuilder;

    /**
     * Send a new collaboration request between garage and technician
     */
    @Transactional
    public RequestingGarageTechnicianResponse sendRequest(
            RequestingGarageTechnicianRequest request,
            SendBy sendBy) {

        log.info("Processing request from {} to link garage {} with technician {}",
                sendBy, request.getGarageId(), request.getTechnicianId());

        // 1. Validate garage exists
        Garage garage = garageRepository.findById(request.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException(GARAGE_NOT_FOUND + request.getGarageId()));

        // 2. Validate technician exists
        Technician technician = technicianRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new ResourceNotFoundException(TECHNICIAN_NOT_FOUND + request.getTechnicianId()));

        // 3. Check if already linked
        if (technician.getGarages() != null && technician.getGarages().contains(garage)) {
            log.warn("Request rejected: Garage {} and Technician {} are already linked",
                    request.getGarageId(), request.getTechnicianId());
            throw new AlreadyExistsException(ALREADY_LINKED);
        }

        // 4. Check for duplicate pending request
        if (requestRepository.existsHoldRequestBetween(request.getGarageId(), request.getTechnicianId())) {
            log.warn("Request rejected: Pending request already exists between garage {} and technician {}",
                    request.getGarageId(), request.getTechnicianId());
            throw new ConflictException(DUPLICATE_REQUEST);
        }

        // 5. Create new request entity
        RequestingGarageAndTechnician entity = new RequestingGarageAndTechnician();
        entity.setGarage(garage);
        entity.setTechnician(technician);
        entity.setSendBy(sendBy);
        entity.setApprovalStatus(ApprovalStatus.HOLD);
        entity.setDescription(request.getDescription());

        // 6. Save and return
        RequestingGarageAndTechnician saved = requestRepository.save(entity);
        log.info("Request created with id: {}", saved.getId());

        // 7. Send notification to recipient
        sendRequestReceivedNotification(garage, technician, sendBy, request.getDescription());

        return mapper.entityToResponse(saved);
    }

    /**
     * Approve a pending request and create the garage-technician relationship
     */
    @Transactional
    public RequestingGarageTechnicianResponse approveRequest(
            Integer requestId,
            Integer approverId,
            SendBy approverType) {

        log.info("Approving request {} by {} with id {}", requestId, approverType, approverId);

        // 1. Fetch request with full entity graph
        RequestingGarageAndTechnician request = requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(REQUEST_NOT_FOUND + requestId));

        // 2. Validate status is HOLD
        if (request.getApprovalStatus() != ApprovalStatus.HOLD) {
            log.warn("Request {} is in {} status, cannot approve", requestId, request.getApprovalStatus());
            throw new BadRequestException(INVALID_STATUS);
        }

        // 3. Validate approver is the recipient
        validateApprover(request, approverId, approverType);

        // 4. Create the relationship by adding to technician's garage set
        Technician technician = request.getTechnician();
        Garage garage = request.getGarage();

        if (technician.getGarages() == null) {
            technician.setGarages(new java.util.HashSet<>());
        }
        technician.getGarages().add(garage);

        // Save technician to persist the many-to-many relationship
        technicianRepository.save(technician);
        log.info("Created link between garage {} and technician {}", garage.getId(), technician.getId());

        // 5. Update request status to ACCEPTED
        request.setApprovalStatus(ApprovalStatus.ACCEPTED);
        RequestingGarageAndTechnician updated = requestRepository.save(request);

        log.info("Request {} approved successfully", requestId);

        // 6. Send notification to the original sender
        sendApprovalNotification(updated);

        return mapper.entityToResponse(updated);
    }

    /**
     * Reject a pending request
     */
    @Transactional
    public RequestingGarageTechnicianResponse rejectRequest(
            Integer requestId,
            Integer rejecterId,
            SendBy rejecterType) {

        log.info("Rejecting request {} by {} with id {}", requestId, rejecterType, rejecterId);

        // 1. Fetch request
        RequestingGarageAndTechnician request = requestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException(REQUEST_NOT_FOUND + requestId));

        // 2. Validate status is HOLD
        if (request.getApprovalStatus() != ApprovalStatus.HOLD) {
            log.warn("Request {} is in {} status, cannot reject", requestId, request.getApprovalStatus());
            throw new BadRequestException(INVALID_STATUS);
        }

        // 3. Validate rejecter is the recipient
        validateApprover(request, rejecterId, rejecterType);

        // 4. Update status to REJECTED
        request.setApprovalStatus(ApprovalStatus.REJECTED);
        RequestingGarageAndTechnician updated = requestRepository.save(request);

        log.info("Request {} rejected successfully", requestId);

        // 5. Send notification to the original sender
        sendRejectionNotification(updated);

        return mapper.entityToResponse(updated);
    }

    /**
     * Get all pending requests for a garage (sent by technicians)
     */
    @Transactional(readOnly = true)
    public List<RequestingGarageTechnicianResponse> getPendingRequestsForGarage(Integer garageId) {
        log.info("Fetching pending requests for garage {}", garageId);

        if (!garageRepository.existsById(garageId)) {
            throw new ResourceNotFoundException(GARAGE_NOT_FOUND + garageId);
        }

        return requestRepository.findPendingRequestsForGarage(garageId)
                .stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Get all pending requests for a technician (sent by garages)
     */
    @Transactional(readOnly = true)
    public List<RequestingGarageTechnicianResponse> getPendingRequestsForTechnician(Integer technicianId) {
        log.info("Fetching pending requests for technician {}", technicianId);

        if (!technicianRepository.existsById(technicianId)) {
            throw new ResourceNotFoundException(TECHNICIAN_NOT_FOUND + technicianId);
        }

        return requestRepository.findPendingRequestsForTechnician(technicianId)
                .stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Get all requests for a garage (all statuses) - for audit trail
     */
    @Transactional(readOnly = true)
    public List<RequestingGarageTechnicianResponse> getRequestHistoryForGarage(Integer garageId) {
        log.info("Fetching request history for garage {}", garageId);

        if (!garageRepository.existsById(garageId)) {
            throw new ResourceNotFoundException(GARAGE_NOT_FOUND + garageId);
        }

        return requestRepository.findAllRequestsByGarage(garageId)
                .stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Get all requests for a technician (all statuses) - for audit trail
     */
    @Transactional(readOnly = true)
    public List<RequestingGarageTechnicianResponse> getRequestHistoryForTechnician(Integer technicianId) {
        log.info("Fetching request history for technician {}", technicianId);

        if (!technicianRepository.existsById(technicianId)) {
            throw new ResourceNotFoundException(TECHNICIAN_NOT_FOUND + technicianId);
        }

        return requestRepository.findAllRequestsByTechnician(technicianId)
                .stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Send notification when a collaboration request is received
     */
    private void sendRequestReceivedNotification(Garage garage, Technician technician,
                                                  SendBy sendBy, String description) {
        try {
            Integer recipientUserId;
            String recipientRole;
            String senderName;
            String senderType;

            if (sendBy == SendBy.GARAGE) {
                // Garage sent request → notify Technician
                recipientUserId = technician.getAutoAddaId();
                recipientRole = GARAGE_TECH;
                senderName = garage.getName();
                senderType = GARAGE;
            } else {
                // Technician sent request → notify Garage Owner
                recipientUserId = garage.getGarageOwner();
                recipientRole = GARAGE_OWNER;
                senderName = technician.getFirstName();
                senderType = TECHNICIAN;
            }

            // Build notification content
            NotificationContent content = notificationMessageBuilder
                    .buildCollaborationRequestReceived(senderName, senderType, description);

            // Send notification (SMS, Email, Push)
            NotificationResult result = notificationDispatchService.sendNotification(
                    recipientUserId,
                    NotificationType.COLLABORATION_REQUESTS,
                    content,
                    false,  // Not critical
                    recipientRole
            );

            log.info("Sent collaboration request notification to {} (userId: {}): {}",
                    recipientRole, recipientUserId, result.getSummary());

        } catch (Exception e) {
            log.error("Failed to send collaboration request notification: {}", e.getMessage(), e);
            // Don't fail the request if notification fails
        }
    }

    /**
     * Send notification when a collaboration request is approved
     */
    private void sendApprovalNotification(RequestingGarageAndTechnician request) {
        try {
            Integer senderUserId;
            String senderRole;
            String approverName;
            String approverType;

            if (request.getSendBy() == SendBy.GARAGE) {
                // Garage sent, Technician approved → notify Garage Owner
                senderUserId = request.getGarage().getGarageOwner();
                senderRole = GARAGE_OWNER;
                approverName = request.getTechnician().getFirstName();
                approverType = TECHNICIAN;
            } else {
                // Technician sent, Garage approved → notify Technician
                senderUserId = request.getTechnician().getAutoAddaId();
                senderRole = GARAGE_TECH;
                approverName = request.getGarage().getName();
                approverType = GARAGE;
            }

            // Build notification content
            NotificationContent content = notificationMessageBuilder
                    .buildCollaborationRequestApproved(approverName, approverType);

            // Send notification
            NotificationResult result = notificationDispatchService.sendNotification(
                    senderUserId,
                    NotificationType.COLLABORATION_REQUESTS,
                    content,
                    false,
                    senderRole
            );

            log.info("Sent approval notification to {} (userId: {}): {}",
                    senderRole, senderUserId, result.getSummary());

        } catch (Exception e) {
            log.error("Failed to send approval notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Send notification when a collaboration request is rejected
     */
    private void sendRejectionNotification(RequestingGarageAndTechnician request) {
        try {
            Integer senderUserId;
            String senderRole;
            String rejecterName;
            String rejecterType;

            if (request.getSendBy() == SendBy.GARAGE) {
                // Garage sent, Technician rejected → notify Garage Owner
                senderUserId = request.getGarage().getGarageOwner();
                senderRole = GARAGE_OWNER;
                rejecterName = request.getTechnician().getFirstName();
                rejecterType = TECHNICIAN;
            } else {
                // Technician sent, Garage rejected → notify Technician
                senderUserId = request.getTechnician().getAutoAddaId();
                senderRole = GARAGE_TECH;
                rejecterName = request.getGarage().getName();
                rejecterType = GARAGE;
            }

            // Build notification content
            NotificationContent content = notificationMessageBuilder
                    .buildCollaborationRequestRejected(rejecterName, rejecterType);

            // Send notification
            NotificationResult result = notificationDispatchService.sendNotification(
                    senderUserId,
                    NotificationType.COLLABORATION_REQUESTS,
                    content,
                    false,
                    senderRole
            );

            log.info("Sent rejection notification to {} (userId: {}): {}",
                    senderRole, senderUserId, result.getSummary());

        } catch (Exception e) {
            log.error("Failed to send rejection notification: {}", e.getMessage(), e);
        }
    }

    /**
     * Validate that the approver/rejecter is the recipient of the request
     */
    private void validateApprover(RequestingGarageAndTechnician request, Integer approverId, SendBy approverType) {
        boolean isValid = false;

        if (request.getSendBy() == SendBy.GARAGE && approverType == SendBy.TECHNICIAN) {
            // Request sent by garage, technician is approving
            isValid = request.getTechnician().getId().equals(approverId);
        } else if (request.getSendBy() == SendBy.TECHNICIAN && approverType == SendBy.GARAGE) {
            // Request sent by technician, garage is approving
            isValid = request.getGarage().getId().equals(approverId);
        }

        if (!isValid) {
            log.warn("Unauthorized approval attempt: {} with id {} trying to approve request {} sent by {}",
                    approverType, approverId, request.getId(), request.getSendBy());
            throw new BadRequestException(UNAUTHORIZED_ACTION);
        }
    }
}
