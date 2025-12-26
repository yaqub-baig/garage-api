package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.CustomerDeliveryApprovalRequest;
import com.autoadda.apis.garage.request.VehicleDeliveryRequest;
import com.autoadda.apis.garage.response.VehicleDeliveryComparisonResponse;
import com.autoadda.apis.garage.response.VehicleDeliveryResponse;
import com.autoadda.apis.garage.response.VehicleDeliveryWithReceivedChecklistResponse;
import com.autoadda.apis.garage.response.VehicleReceivedChecklistResponse;
import com.autoadda.apis.garage.util.DeliveryStatus;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleDeliveryService {

    public static final String VEHICLE_DELIVERY_NOT_FOUND = "Vehicle delivery not found";
    private final VehicleDeliveryRepository vehicleDeliveryRepo;
    private final WorkOrderRepository workOrderRepo;
    private final TechnicianRepository technicianRepo;
    private final MediaFileRepository mediaFileRepo;
    private final VehicleReceivedRepository vehicleReceivedRepo;

    /**
     * Initiate delivery process for a work order
     */
    @Transactional
    public VehicleDeliveryResponse initiateDelivery(Integer workOrderId, Integer technicianId) {
        log.info("Initiating delivery for work order: {}", workOrderId);

        // Validate work order exists
        WorkOrder workOrder = workOrderRepo.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found"));

        // Validate status
        if (workOrder.getStatus() != WorkOrderStatus.READY_FOR_DELIVERY) {
            throw new IllegalStateException(
                    "Work order must be in READY_FOR_DELIVERY status. Current status: " + workOrder.getStatus());
        }

        // Check if there's an active delivery (non-terminal status)
        if (!workOrder.canInitiateDelivery()) {
            throw new IllegalStateException("Delivery already in progress for this work order");
        }

        // Get technician
        Technician technician = technicianRepo.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found"));

        // Calculate attempt number based on previous deliveries
        List<VehicleDelivery> previousDeliveries = vehicleDeliveryRepo.findAllByWorkOrderId(workOrderId);
        int attemptNumber = previousDeliveries.size() + 1;

        // Create delivery record
        VehicleDelivery delivery = VehicleDelivery.builder()
                .workOrder(workOrder)
                .deliveredBy(technician)
                .status(DeliveryStatus.PENDING_VERIFICATION)
                .customerApproved(false)
                .deliveryDate(LocalDateTime.now())
                .attemptNumber(attemptNumber)
                .build();

        delivery = vehicleDeliveryRepo.save(delivery);

        log.info("Delivery initiated successfully. Delivery ID: {}, Attempt: {}", delivery.getId(), attemptNumber);

        return convertToResponse(delivery);
    }

    /**
     * Submit delivery checklist with all item verifications
     */
    @Transactional
    public VehicleDeliveryResponse submitDeliveryChecklist(Integer deliveryId, VehicleDeliveryRequest request) {
        log.info("Submitting delivery checklist for delivery ID: {}", deliveryId);

        VehicleDelivery delivery = vehicleDeliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(VEHICLE_DELIVERY_NOT_FOUND));

        // Validate status
        if (delivery.getStatus() != DeliveryStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Delivery checklist already submitted");
        }

        // Validate all 12 items are provided
        validateChecklistComplete(request);

        // Update item verification fields
        delivery.setIsMainKeyReturned(request.getIsMainKeyReturned());
        delivery.setIsSpareKeyReturned(request.getIsSpareKeyReturned());
        delivery.setIsToolKitReturned(request.getIsToolKitReturned());
        delivery.setIsJackReturned(request.getIsJackReturned());
        delivery.setIsWheelSpannerReturned(request.getIsWheelSpannerReturned());
        delivery.setIsSpareTyreReturned(request.getIsSpareTyreReturned());
        delivery.setIsFireExtinguisherReturned(request.getIsFireExtinguisherReturned());
        delivery.setIsFirstAidKitReturned(request.getIsFirstAidKitReturned());
        delivery.setIsCentralLockRemoteReturned(request.getIsCentralLockRemoteReturned());
        delivery.setIsRCBookReturned(request.getIsRCBookReturned());
        delivery.setIsInsuranceCopyReturned(request.getIsInsuranceCopyReturned());
        delivery.setIsPUCReturned(request.getIsPUCReturned());

        // Handle delivery photos
        if (request.getDeliveryPhotos() != null && !request.getDeliveryPhotos().isEmpty()) {
            List<MediaFile> mediaFiles = new ArrayList<>(mediaFileRepo.findAllById(request.getDeliveryPhotos()));
            if (mediaFiles.size() != request.getDeliveryPhotos().size()) {
                throw new ResourceNotFoundException("Some media files not found");
            }
            delivery.setDeliveryPhotos(mediaFiles);
            log.info("Added {} delivery photos", mediaFiles.size());
        }

        // Update comments if provided
        if (request.getComments() != null && !request.getComments().trim().isEmpty()) {
            delivery.setCustomerComments(request.getComments());
        }

        // Change status to pending customer approval
        delivery.setStatus(DeliveryStatus.PENDING_CUSTOMER_APPROVAL);

        delivery = vehicleDeliveryRepo.save(delivery);

        log.info("Delivery checklist submitted successfully. Status: PENDING_CUSTOMER_APPROVAL");

        return convertToResponse(delivery);
    }

    /**
     * Get comparison between receiving and delivery checklists
     */
    public VehicleDeliveryComparisonResponse getDeliveryComparison(Integer workOrderId) {
        log.info("Getting delivery comparison for work order: {}", workOrderId);

        // Get work order
        WorkOrder workOrder = workOrderRepo.findById(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found"));

        // Get vehicle received record
        VehicleReceived vehicleReceived = vehicleReceivedRepo.findByAppointmentId(
                workOrder.getAppointment().getId());
        if (vehicleReceived == null) {
            throw new ResourceNotFoundException("Vehicle receiving record not found");
        }

        // Get latest delivery record
        VehicleDelivery delivery = vehicleDeliveryRepo.findTopByWorkOrderIdOrderByCreatedAtDesc(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException(VEHICLE_DELIVERY_NOT_FOUND));

        // Build receiving checklist
        VehicleDeliveryComparisonResponse.ItemsChecklist receivingChecklist =
                VehicleDeliveryComparisonResponse.ItemsChecklist.builder()
                        .isMainKey(vehicleReceived.getIsMainKeyReceived())
                        .isSpareKey(vehicleReceived.getIsSpareKeyReceived())
                        .isToolKit(vehicleReceived.getIsToolKitReceived())
                        .isJack(vehicleReceived.getIsJackReceived())
                        .isWheelSpanner(vehicleReceived.getIsWheelSpannerReceived())
                        .isSpareTyre(vehicleReceived.getIsSpareTyreReceived())
                        .isFireExtinguisher(vehicleReceived.getIsFireExtinguisherReceived())
                        .isFirstAidKit(vehicleReceived.getIsFirstAidKitReceived())
                        .isCentralLockRemote(vehicleReceived.getIsCentralLockRemoteReceived())
                        .isRCBook(vehicleReceived.getIsRCBookReceived())
                        .isInsuranceCopy(vehicleReceived.getIsInsuranceCopyReceived())
                        .isPUC(vehicleReceived.getIsPUCReceived())
                        .build();

        // Build delivery checklist
        VehicleDeliveryComparisonResponse.ItemsChecklist deliveryChecklist =
                VehicleDeliveryComparisonResponse.ItemsChecklist.builder()
                        .isMainKey(delivery.getIsMainKeyReturned())
                        .isSpareKey(delivery.getIsSpareKeyReturned())
                        .isToolKit(delivery.getIsToolKitReturned())
                        .isJack(delivery.getIsJackReturned())
                        .isWheelSpanner(delivery.getIsWheelSpannerReturned())
                        .isSpareTyre(delivery.getIsSpareTyreReturned())
                        .isFireExtinguisher(delivery.getIsFireExtinguisherReturned())
                        .isFirstAidKit(delivery.getIsFirstAidKitReturned())
                        .isCentralLockRemote(delivery.getIsCentralLockRemoteReturned())
                        .isRCBook(delivery.getIsRCBookReturned())
                        .isInsuranceCopy(delivery.getIsInsuranceCopyReturned())
                        .isPUC(delivery.getIsPUCReturned())
                        .build();

        // Find mismatches
        List<String> mismatches = findMismatches(vehicleReceived, delivery);

        // Get photo URLs
        List<String> receivingPhotos = vehicleReceived.getVehiclePhotos().stream()
                .map(MediaFile::getFilePath)
                .toList();

        List<String> deliveryPhotos = delivery.getDeliveryPhotos().stream()
                .map(MediaFile::getFilePath)
                .toList();

        return VehicleDeliveryComparisonResponse.builder()
                .deliveryId(delivery.getId())
                .workOrderId(workOrderId)
                .receivingChecklist(receivingChecklist)
                .deliveryChecklist(deliveryChecklist)
                .mismatches(mismatches)
                .receivingPhotos(receivingPhotos)
                .deliveryPhotos(deliveryPhotos)
                .build();
    }

    /**
     * Submit customer approval or rejection
     */
    @Transactional
    public VehicleDeliveryResponse submitCustomerApproval(Integer deliveryId,
                                                          CustomerDeliveryApprovalRequest request) {
        log.info("Processing customer approval for delivery ID: {}. Approved: {}", deliveryId, request.getApproved());

        VehicleDelivery delivery = vehicleDeliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(VEHICLE_DELIVERY_NOT_FOUND));

        // Validate status
        if (delivery.getStatus() != DeliveryStatus.PENDING_CUSTOMER_APPROVAL) {
            throw new IllegalStateException("Delivery is not pending customer approval");
        }

        // Validate rejection reason if rejected
        if (Boolean.FALSE.equals(request.getApproved()) && (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty())) {
                throw new IllegalArgumentException("Rejection reason is required when rejecting delivery");
            }


        WorkOrder workOrder = delivery.getWorkOrder();

        if (Boolean.TRUE.equals(request.getApproved())) {
            // Customer approved delivery
            delivery.setCustomerApproved(true);
            delivery.setStatus(DeliveryStatus.APPROVED);
            delivery.setCustomerComments(request.getCustomerComments());

            // Update work order status
            workOrder.updateStatus(WorkOrderStatus.DELIVERED_TO_CUSTOMER);
            workOrder.setWorkCompletedDate(LocalDateTime.now());
            workOrderRepo.save(workOrder);

            log.info("Customer approved delivery. Work order {} status updated to DELIVERED_TO_CUSTOMER",
                    workOrder.getId());
            log.info("Work completed date set to: {}", workOrder.getWorkCompletedDate());

        } else {
            // Customer rejected delivery
            delivery.setCustomerApproved(false);
            delivery.setStatus(DeliveryStatus.REJECTED);
            delivery.setRejectionReason(request.getRejectionReason());
            delivery.setCustomerComments(request.getCustomerComments());

            // Revert work order status back to READY_FOR_DELIVERY
            workOrder.updateStatus(WorkOrderStatus.READY_FOR_DELIVERY);
            workOrderRepo.save(workOrder);

            log.warn("Customer REJECTED delivery for work order {}. Reason: {}",
                    workOrder.getId(), request.getRejectionReason());
            log.info("Work order status reverted to READY_FOR_DELIVERY");

            // TODO: Send notification to garage manager about rejection
        }

        delivery = vehicleDeliveryRepo.save(delivery);

        return convertToResponse(delivery);
    }

    /**
     * Get delivery details by work order ID (returns the latest delivery)
     */
    public VehicleDeliveryWithReceivedChecklistResponse getDeliveryByWorkOrderId(Integer workOrderId) {
        log.info("Fetching delivery details for work order: {}", workOrderId);

        VehicleDelivery delivery = vehicleDeliveryRepo.findTopByWorkOrderIdOrderByCreatedAtDesc(workOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle delivery not found for this work order"));
        VehicleReceived vehicleReceived = vehicleReceivedRepo.findByAppointmentId(delivery.getWorkOrder().getAppointment().getId());
        return convertToDeliveryAndReceivedChecklistResponse(delivery, vehicleReceived);
    }

    /**
     * Get delivery details by delivery ID
     */
    public VehicleDeliveryResponse getDeliveryById(Integer deliveryId) {
        log.info("Fetching delivery details by ID: {}", deliveryId);

        VehicleDelivery delivery = vehicleDeliveryRepo.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException(VEHICLE_DELIVERY_NOT_FOUND));

        return convertToResponse(delivery);
    }

    /**
     * Validate that all 12 checklist items are provided
     */
    private void validateChecklistComplete(VehicleDeliveryRequest request) {
        List<String> missingFields = new ArrayList<>();

        if (request.getIsMainKeyReturned() == null) missingFields.add("Main Key");
        if (request.getIsSpareKeyReturned() == null) missingFields.add("Spare Key");
        if (request.getIsToolKitReturned() == null) missingFields.add("Tool Kit");
        if (request.getIsJackReturned() == null) missingFields.add("Jack");
        if (request.getIsWheelSpannerReturned() == null) missingFields.add("Wheel Spanner");
        if (request.getIsSpareTyreReturned() == null) missingFields.add("Spare Tyre");
        if (request.getIsFireExtinguisherReturned() == null) missingFields.add("Fire Extinguisher");
        if (request.getIsFirstAidKitReturned() == null) missingFields.add("First Aid Kit");
        if (request.getIsCentralLockRemoteReturned() == null) missingFields.add("Central Lock Remote");
        if (request.getIsRCBookReturned() == null) missingFields.add("RC Book");
        if (request.getIsInsuranceCopyReturned() == null) missingFields.add("Insurance Copy");
        if (request.getIsPUCReturned() == null) missingFields.add("PUC");

        if (!missingFields.isEmpty()) {
            throw new IllegalArgumentException("Missing checklist items: " + String.join(", ", missingFields));
        }

        // Validate at least one photo
        if (request.getDeliveryPhotos() == null || request.getDeliveryPhotos().isEmpty()) {
            throw new IllegalArgumentException("At least one delivery photo is required");
        }
    }

    /**
     * Find mismatches between receiving and delivery checklists
     */
    private List<String> findMismatches(VehicleReceived received, VehicleDelivery delivery) {
        List<String> mismatches = new ArrayList<>();

        checkItemMismatch("Main Key",
                received.getIsMainKeyReceived(),
                delivery.getIsMainKeyReturned(),
                mismatches);

        checkItemMismatch("Spare Key",
                received.getIsSpareKeyReceived(),
                delivery.getIsSpareKeyReturned(),
                mismatches);

        checkItemMismatch("Tool Kit",
                received.getIsToolKitReceived(),
                delivery.getIsToolKitReturned(),
                mismatches);

        checkItemMismatch("Jack",
                received.getIsJackReceived(),
                delivery.getIsJackReturned(),
                mismatches);

        checkItemMismatch("Wheel Spanner",
                received.getIsWheelSpannerReceived(),
                delivery.getIsWheelSpannerReturned(),
                mismatches);

        checkItemMismatch("Spare Tyre",
                received.getIsSpareTyreReceived(),
                delivery.getIsSpareTyreReturned(),
                mismatches);

        checkItemMismatch("Fire Extinguisher",
                received.getIsFireExtinguisherReceived(),
                delivery.getIsFireExtinguisherReturned(),
                mismatches);

        checkItemMismatch("First Aid Kit",
                received.getIsFirstAidKitReceived(),
                delivery.getIsFirstAidKitReturned(),
                mismatches);

        checkItemMismatch("Central Lock Remote",
                received.getIsCentralLockRemoteReceived(),
                delivery.getIsCentralLockRemoteReturned(),
                mismatches);

        checkItemMismatch("RC Book",
                received.getIsRCBookReceived(),
                delivery.getIsRCBookReturned(),
                mismatches);

        checkItemMismatch("Insurance Copy",
                received.getIsInsuranceCopyReceived(),
                delivery.getIsInsuranceCopyReturned(),
                mismatches);

        checkItemMismatch("PUC",
                received.getIsPUCReceived(),
                delivery.getIsPUCReturned(),
                mismatches);

        return mismatches;
    }

    /**
     * Checks for mismatches between received and returned status of a vehicle item.
     * Adds mismatch messages to the list if:
     * 1. Item not received but being returned
     * 2. Item received but not being returned
     *
     * @param itemName The display name of the item
     * @param received Whether the item was received (from VehicleReceived)
     * @param returned Whether the item is being returned (from VehicleDelivery)
     * @param mismatches The list to add mismatch messages to
     */
    private void checkItemMismatch(String itemName, Boolean received, Boolean returned,
                                   List<String> mismatches) {
        if (!Boolean.TRUE.equals(received) && Boolean.TRUE.equals(returned)) {
            mismatches.add(itemName + ": Not received but being returned");
        }
        if (Boolean.TRUE.equals(received) && !Boolean.TRUE.equals(returned)) {
            mismatches.add(itemName + ": Received but not being returned");
        }
    }

    /**
     * Convert entity to response DTO
     */
    private VehicleDeliveryResponse convertToResponse(VehicleDelivery delivery) {
        List<String> photoUrls = delivery.getDeliveryPhotos().stream()
                .map(MediaFile::getFilePath)
                .toList();

        String technicianName = delivery.getDeliveredBy() != null ?
                delivery.getDeliveredBy().getFirstName() : null;

        return VehicleDeliveryResponse.builder()
                .id(delivery.getId())
                .workOrderId(delivery.getWorkOrder().getId())
                .deliveredByTechnicianId(delivery.getDeliveredBy() != null ? delivery.getDeliveredBy().getId() : null)
                .deliveredByTechnicianName(technicianName)
                .status(delivery.getStatus())
                .attemptNumber(delivery.getAttemptNumber())
                .deliveryDate(delivery.getDeliveryDate())
                .customerApproved(delivery.getCustomerApproved())
                .customerComments(delivery.getCustomerComments())
                .rejectionReason(delivery.getRejectionReason())
                .deliveryPhotos(photoUrls)
                .isMainKeyReturned(delivery.getIsMainKeyReturned())
                .isSpareKeyReturned(delivery.getIsSpareKeyReturned())
                .isToolKitReturned(delivery.getIsToolKitReturned())
                .isJackReturned(delivery.getIsJackReturned())
                .isWheelSpannerReturned(delivery.getIsWheelSpannerReturned())
                .isSpareTyreReturned(delivery.getIsSpareTyreReturned())
                .isFireExtinguisherReturned(delivery.getIsFireExtinguisherReturned())
                .isFirstAidKitReturned(delivery.getIsFirstAidKitReturned())
                .isCentralLockRemoteReturned(delivery.getIsCentralLockRemoteReturned())
                .isRCBookReturned(delivery.getIsRCBookReturned())
                .isInsuranceCopyReturned(delivery.getIsInsuranceCopyReturned())
                .isPUCReturned(delivery.getIsPUCReturned())
                .build();
    }

    /**
     * Convert entity to initiation response DTO with vehicle received checklist
     */
    private VehicleDeliveryWithReceivedChecklistResponse convertToDeliveryAndReceivedChecklistResponse(VehicleDelivery delivery,
                                                                                                       VehicleReceived vehicleReceived) {
        List<String> photoUrls = delivery.getDeliveryPhotos().stream()
                .map(MediaFile::getFilePath)
                .toList();

        String technicianName = delivery.getDeliveredBy() != null ?
                delivery.getDeliveredBy().getFirstName() : null;

        // Build vehicle received checklist
        VehicleReceivedChecklistResponse receivedChecklist = VehicleReceivedChecklistResponse.builder()
                .isMainKeyReceived(vehicleReceived.getIsMainKeyReceived())
                .isSpareKeyReceived(vehicleReceived.getIsSpareKeyReceived())
                .isToolKitReceived(vehicleReceived.getIsToolKitReceived())
                .isJackReceived(vehicleReceived.getIsJackReceived())
                .isWheelSpannerReceived(vehicleReceived.getIsWheelSpannerReceived())
                .isSpareTyreReceived(vehicleReceived.getIsSpareTyreReceived())
                .isFireExtinguisherReceived(vehicleReceived.getIsFireExtinguisherReceived())
                .isFirstAidKitReceived(vehicleReceived.getIsFirstAidKitReceived())
                .isCentralLockRemoteReceived(vehicleReceived.getIsCentralLockRemoteReceived())
                .isRCBookReceived(vehicleReceived.getIsRCBookReceived())
                .isInsuranceCopyReceived(vehicleReceived.getIsInsuranceCopyReceived())
                .isPUCReceived(vehicleReceived.getIsPUCReceived())
                .build();

        return VehicleDeliveryWithReceivedChecklistResponse.builder()
                .id(delivery.getId())
                .workOrderId(delivery.getWorkOrder().getId())
                .deliveredByTechnicianId(delivery.getDeliveredBy() != null ? delivery.getDeliveredBy().getId() : null)
                .deliveredByTechnicianName(technicianName)
                .status(delivery.getStatus())
                .attemptNumber(delivery.getAttemptNumber())
                .deliveryDate(delivery.getDeliveryDate())
                .customerApproved(delivery.getCustomerApproved())
                .customerComments(delivery.getCustomerComments())
                .rejectionReason(delivery.getRejectionReason())
                .deliveryPhotos(photoUrls)
                .isMainKeyReturned(delivery.getIsMainKeyReturned())
                .isSpareKeyReturned(delivery.getIsSpareKeyReturned())
                .isToolKitReturned(delivery.getIsToolKitReturned())
                .isJackReturned(delivery.getIsJackReturned())
                .isWheelSpannerReturned(delivery.getIsWheelSpannerReturned())
                .isSpareTyreReturned(delivery.getIsSpareTyreReturned())
                .isFireExtinguisherReturned(delivery.getIsFireExtinguisherReturned())
                .isFirstAidKitReturned(delivery.getIsFirstAidKitReturned())
                .isCentralLockRemoteReturned(delivery.getIsCentralLockRemoteReturned())
                .isRCBookReturned(delivery.getIsRCBookReturned())
                .isInsuranceCopyReturned(delivery.getIsInsuranceCopyReturned())
                .isPUCReturned(delivery.getIsPUCReturned())
                .vehicleReceivedChecklist(receivedChecklist)
                .build();
    }
}
