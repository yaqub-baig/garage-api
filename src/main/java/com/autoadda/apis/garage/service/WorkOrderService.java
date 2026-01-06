package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.exception.AlreadyExistsException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.WorkOrderMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.AssignServiceRequest;
import com.autoadda.apis.garage.request.NotificationContent;
import com.autoadda.apis.garage.request.WorkOrderRequest;
import com.autoadda.apis.garage.response.*;
import com.autoadda.apis.garage.util.JobCardStatus;
import com.autoadda.apis.garage.util.NotificationType;
import com.autoadda.apis.garage.util.VehicleRecievedStatus;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkOrderService {
    private final AuthClient authClient;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMapper workOrderMapper;
    private final JobCardRepository jobCardRepository;
    private final JobCardService jobCardService;
    private final AppointmentDetailsRepository appointmentDetailsRepository;
    private final VehicleReceivedRepository vehicleReceivedRepository;
    private final JobCardStatusFlowService statusFlowService;
    private final WorkOrderStatusFlowService workOrderStatusFlowService;
    private final InvoiceRepository invoiceRepository;
    private final NotificationDispatchService notificationDispatchService;
    private final NotificationMessageBuilder notificationMessageBuilder;
    private final VehicleDeliveryRepository vehicleDeliveryRepository;


    /**
     * Creates a new work order with the provided job cards.
     *
     * @param jobCards List of JobCard objects to be included in the work order.
     * @return The created WorkOrder object.
     */
    @Transactional
    public WorkOrder createWorkOrder(List<JobCard> jobCards) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setJobCards(jobCards);
        workOrder.setStatus(WorkOrderStatus.CREATED);
        workOrder.setCreatedDate(LocalDateTime.now());
        return workOrderRepository.save(workOrder);
    }

    public WorkOrderResponse createWorkOrder(WorkOrderRequest request) {
        checkAppointmentAlreadyExists(request.getAppointmentId());
        WorkOrder workOrder = new WorkOrder();
        workOrder.setStatus(WorkOrderStatus.CREATED);
        workOrder.setCreatedDate(LocalDateTime.now());
        workOrder.setAppointment(appointmentDetailsRepository.findById(request.getAppointmentId()).orElseThrow(() -> new ResourceNotFoundException("appointment not found")));
        workOrder.setGarage(workOrder.getAppointment().getGarage());
        workOrder = workOrderRepository.save(workOrder);

        VehicleReceived byAppointmentId = vehicleReceivedRepository.findByAppointmentId(workOrder.getAppointment().getId());
        byAppointmentId.setStatus(VehicleRecievedStatus.JOB_CARD_CREATED);
        vehicleReceivedRepository.saveAndFlush(byAppointmentId);
        WorkOrderResponse workOrderResponse = workOrderMapper.entityToResponse(workOrderRepository.save(workOrder));

        List<JobCard> list = request.getAssignServiceRequestList().stream().map(request1 -> jobCardService.assignTechnicianToServiceWithOutResponse(request1, workOrderResponse.getId())).toList();
        workOrder.setJobCards(list);

        WorkOrder finalWorkOrder = workOrder;
        list.forEach(jobCard -> jobCard.setWorkOrder(finalWorkOrder));

        // Send multi-channel notification with preference checking
        Integer customerId = workOrderResponse.getAppointmentDetail().getCustomerId();
        String vehicleNumber = workOrderResponse.getAppointmentDetail().getVehicle().getNumber();
        int serviceCount = workOrder.getJobCards().size();

        NotificationContent content = notificationMessageBuilder.buildWorkOrderCreated(
                workOrderResponse.getId(),
                vehicleNumber,
                serviceCount
        );

        NotificationResult result = notificationDispatchService.sendNotification(
                customerId,
                NotificationType.SERVICE_UPDATES,
                content,
                true,  // critical
                "CUSTOMER"  // targetRole
        );

        log.info("Work order creation notification result: {}", result.getSummary());

        // Send notification to garage owner about new work order
        try {
            Garage garage = workOrder.getGarage();
            if (garage != null && garage.getGarageOwner() != null) {
                NotificationContent ownerContent = notificationMessageBuilder
                        .buildGarageOwnerWorkOrder(
                                workOrderResponse.getId(),
                                vehicleNumber,
                                serviceCount,
                                garage.getName()
                        );

                NotificationResult ownerResult = notificationDispatchService.sendNotification(
                        garage.getGarageOwner(),
                        NotificationType.SERVICE_UPDATES,
                        ownerContent,
                        false,  // Not critical for owner
                        "GARAGE_OWNER"  // targetRole
                );

                log.info("Garage owner notification result: {}", ownerResult.getSummary());
            } else {
                log.warn("Cannot send garage owner notification: garage or garageOwner is null");
            }
        } catch (Exception e) {
            log.error("Failed to send garage owner notification", e);
        }

        return workOrderResponse;

    }

    private void checkAppointmentAlreadyExists(Integer appointmentId) {
        if (workOrderRepository.existsByAppointmentId(appointmentId)) {
            throw new AlreadyExistsException("work order with this appointment Id :: " + appointmentId + " alreadyExist");
        }
    }

    @Transactional
    public WorkOrder getWorkOrderById(Integer id) {
        return workOrderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("WorkOrder not found"));
    }

    /**
     * Retrieves all work orders with the specified status.
     *
     * @param status The status of the work orders to retrieve.
     * @return A list of WorkOrder objects with the specified status.
     */
    @Transactional
    public List<WorkOrder> getWorkOrdersByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    /**
     * Updates the status of a work order.
     *
     * @param id     The ID of the work order to update.
     * @param status The new status to set for the work order.
     * @return The updated WorkOrder object.
     */
    @Transactional
    public WorkOrder updateWorkOrderStatus(Integer id, WorkOrderStatus status) {
        WorkOrder workOrder = getWorkOrderById(id);
        workOrder.updateStatus(status);
        return workOrderRepository.save(workOrder);
    }

    @Transactional
    public WorkOrder updateWorkOrderStatus(WorkOrder workOrder, WorkOrderStatus status) {
        workOrder.updateStatus(status);
        return workOrderRepository.save(workOrder);
    }

    /**
     * Retrieves all work orders associated with a specific garage.
     *
     * @param garageId The ID of the garage for which to retrieve work orders.
     * @return A list of WorkOrder objects associated with the specified garage.
     */

    public List<WorkOrder> getWorkOrdersForGarage(Integer garageId) {
        return workOrderRepository.findByGarageId(garageId);
    }

    public WorkOrderResponse getWorkOrderByAppointmentId(Integer appointmentId) {
        return workOrderMapper.entityToResponse(workOrderRepository.findByAppointmentId(appointmentId).orElseThrow(() -> new ResourceNotFoundException("work order not found")));
    }

    public List<WorkOrderResponse> getWorkOrdersByGarageId(Integer garageId) {
        return workOrderRepository.findByGarageId(garageId)
                .stream()
                .sorted(Comparator.comparing(WorkOrder::getCreatedDate).reversed())
                .map(workOrderMapper::entityToResponse).toList();
    }

    public WorkOrderResponse updateWorkOrder(Integer workOrderId, WorkOrderRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElseThrow(() -> new ResourceNotFoundException("no work order found with this id"));
        if (request.getAssignServiceRequestList() != null && !request.getAssignServiceRequestList().isEmpty()) {
            List<JobCard> jobCards = request.getAssignServiceRequestList().stream().map((AssignServiceRequest request1) -> jobCardService.assignTechnicianToServiceWithOutResponse(request1, workOrder.getId())).toList();
            workOrder.getJobCards().addAll(jobCards);
            jobCards.forEach(jobCard -> jobCard.setWorkOrder(workOrder));
        }
        if (request.getCustomerAcknowledged() != null) {
            workOrder.setCustomerAcknowledged(request.getCustomerAcknowledged());
        }
        if (request.getCustomerNegotiatedAmount() != null) {
            workOrder.setCustomerNegotiatedAmount(request.getCustomerNegotiatedAmount());
        }
        if (request.getCustomerNegotiatedComment() != null) {
            workOrder.setCustomerNegotiatedComment(request.getCustomerNegotiatedComment());
        }
        if (request.getGarageNegotiatedAmount() != null) {
            workOrder.setGarageNegotiatedAmount(request.getGarageNegotiatedAmount());
        }
        if (request.getGarageNegotiatedComment() != null) {
            workOrder.setGarageNegotiatedComment(request.getGarageNegotiatedComment());
        }
        if (request.getStatus() != null) {
            if (!workOrderStatusFlowService.isValidTransition(workOrder.getStatus(), request.getStatus())) {
                throw new IllegalStateException("Invalid transition from " +
                        workOrder.getStatus() + " to " + request.getStatus());
            }
            if (request.getStatus() == WorkOrderStatus.CONFIRMED) {
                handleWorkOrderConfirmation(workOrder);
            }
            workOrder.updateStatus(request.getStatus());
        }

        return workOrderMapper.entityToResponse(workOrderRepository.save(workOrder));
    }

    /**
     * Handles the confirmation of a work order by updating associated job card statuses
     */
    private void handleWorkOrderConfirmation(WorkOrder workOrder) {
        workOrder.getJobCards().stream()
                .map(JobCard::getId)
                .forEach(id -> {
                    JobCard jobCard = jobCardRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("no job card found with this id"));
                    if (jobCard.getStatus() == JobCardStatus.CREATED) {
                        jobCardService.updateJobCardStatus(jobCard.getId(), JobCardStatus.ASSIGNED);
                    }
                });
    }

    public List<WorkOrderResponse> getWorkOrdersByCustomerId(Integer customerId) {
        return workOrderRepository.findByAppointmentCustomerId(customerId)
                .stream()
                .sorted(Comparator.comparing(WorkOrder::getCreatedDate).reversed())
                .map(workOrderMapper::entityToResponse)
                .toList();
    }

    public List<VehicleServiceHistoryResponse> getWorkOrdersByVehicleId(Integer vehicleId) {
        return workOrderRepository.findByAppointmentVehicleId(vehicleId)
                .stream()
                .filter(workOrder -> workOrder.getStatus() == WorkOrderStatus.DELIVERED_TO_CUSTOMER
                        || workOrder.getStatus() == WorkOrderStatus.COMPLETED)
                .sorted(Comparator.comparing(WorkOrder::getCreatedDate).reversed())
                .map(this::convertToServiceHistoryResponse)
                .toList();
    }

    /**
     * Convert WorkOrder entity to VehicleServiceHistoryResponse
     */
    private VehicleServiceHistoryResponse convertToServiceHistoryResponse(WorkOrder workOrder) {
        // Extract service names from job cards
        List<String> servicesPerformed = workOrder.getJobCards().stream()
                .map(JobCard::getService)
                .filter(appointmentService -> appointmentService != null
                        && appointmentService.getService() != null
                        && appointmentService.getService().getAvailableService() != null)
                .map(appointmentService -> appointmentService.getService().getAvailableService().getName())
                .toList();

        // Build garage basic info
        GarageBasicInfo garageInfo = null;
        if (workOrder.getGarage() != null) {
            garageInfo = GarageBasicInfo.builder()
                    .id(workOrder.getGarage().getId())
                    .garageBrandName(workOrder.getGarage().getGarageBrandName())
                    .name(workOrder.getGarage().getName())
                    .phoneNumber(workOrder.getGarage().getPhoneNumber())
                    .mobileNumber(workOrder.getGarage().getMobileNumber())
                    .location(workOrder.getGarage().getGarageLocation())
                    .build();
        }

        // Get invoice if exists
        InvoiceResponse invoiceResponse = null;
        Invoice invoice = invoiceRepository.findByWorkOrderId(workOrder.getId()).orElse(null);
        if (invoice != null) {
            invoiceResponse = InvoiceResponse.builder()
                    .id(invoice.getId())
                    .workOrderId(invoice.getWorkOrder().getId())
                    .totalAmount(invoice.getTotalAmount())
                    .taxRate(invoice.getTaxRate())
                    .taxAmount(invoice.getTaxAmount())
                    .amountPaid(invoice.getAmountPaid())
                    .loyaltyPointsUsed(invoice.getLoyaltyPointsUsed())
                    .loyaltyPointsEarned(invoice.getLoyaltyPointsEarned())
                    .paymentStatus(invoice.getPaymentStatus())
                    .paymentMethod(invoice.getPaymentMethod())
                    .isPaid(invoice.getIsPaid())
                    .notes(invoice.getNotes())
                    .createdAt(invoice.getCreatedAt())
                    .build();
        }

        // Get latest delivery if exists
        LatestDeliveryInfo latestDelivery = null;
        VehicleDelivery delivery = vehicleDeliveryRepository.findTopByWorkOrderIdOrderByCreatedAtDesc(workOrder.getId())
                .orElse(null);
        if (delivery != null) {
            List<String> photoUrls = delivery.getDeliveryPhotos().stream()
                    .map(MediaFile::getFilePath)
                    .toList();

            latestDelivery = LatestDeliveryInfo.builder()
                    .id(delivery.getId())
                    .status(delivery.getStatus())
                    .deliveryDate(delivery.getDeliveryDate())
                    .customerApproved(delivery.getCustomerApproved())
                    .customerComments(delivery.getCustomerComments())
                    .rejectionReason(delivery.getRejectionReason())
                    .attemptNumber(delivery.getAttemptNumber())
                    .deliveryPhotos(photoUrls)
                    .build();
        }

        // Build and return the service history response
        return VehicleServiceHistoryResponse.builder()
                .workOrderId(workOrder.getId())
                .serviceDate(workOrder.getCreatedDate())
                .status(workOrder.getStatus())
                .garage(garageInfo)
                .servicesPerformed(servicesPerformed)
                .invoice(invoiceResponse)
                .latestDelivery(latestDelivery)
                .build();
    }

    public WorkOrderResponse getWorkOrderResponseById(Integer workOrderId) {
        WorkOrder workOrder = getWorkOrderById(workOrderId);
        return workOrderMapper.entityToResponse(workOrder);
    }

    public void updateJobCardStatus(JobCard jobCard, JobCardStatus newStatus) {
        if (!statusFlowService.isValidTransition(jobCard.getStatus(), newStatus)) {
            throw new IllegalStateException("Invalid transition from " +
                    jobCard.getStatus() + " to " + newStatus);
        }
        JobCardStatus oldStatus = jobCard.getStatus();
        jobCard.setStatus(newStatus);

        // Update timestamps based on status
        switch (newStatus) {
            case ASSIGNED:
                jobCard.setAssignedAt(LocalDateTime.now());
                break;
            case IN_PROGRESS:
                if (oldStatus == JobCardStatus.ASSIGNED) {
                    jobCard.setStartedAt(LocalDateTime.now());
                }
                break;
            case WORK_COMPLETED:
                jobCard.setCompletedAt(LocalDateTime.now());
                break;
            case QUALITY_CHECK_PASSED, QUALITY_CHECK_FAILED:
                jobCard.setQualityCheckedAt(LocalDateTime.now());
                break;
            default:
                // No timestamp update needed for other statuses
                break;
        }

        // Update WorkOrder status based on JobCard statuses
        workOrderStatusFlowService.updateWorkOrderStatusBasedOnJobCards(jobCard.getWorkOrder());
    }


    public List<JobCard> getOverdueJobCards() {
        // Implementation to find overdue job cards
        return new ArrayList<>();
    }

    public void assignJobCard(JobCard jobCard) {
        // Set technician and update status
        updateJobCardStatus(jobCard, JobCardStatus.ASSIGNED);
    }

    public void startJobCard(JobCard jobCard) {
        updateJobCardStatus(jobCard, JobCardStatus.IN_PROGRESS);
    }

    public void completeJobCard(JobCard jobCard) {
        updateJobCardStatus(jobCard, JobCardStatus.WORK_COMPLETED);
    }

    public void performQualityCheck(JobCard jobCard, boolean passed, String comments) {
        jobCard.setQualityComments(comments);
        updateJobCardStatus(jobCard, passed ?
                JobCardStatus.QUALITY_CHECK_PASSED : JobCardStatus.QUALITY_CHECK_FAILED);
    }


}
