package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.VehicleReceived;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.WorkOrderMapper;
import com.autoadda.apis.garage.repository.AppointmentDetailsRepository;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.repository.VehicleReceivedRepository;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.request.AssignServiceRequest;
import com.autoadda.apis.garage.request.SmsRequest;
import com.autoadda.apis.garage.request.WorkOrderRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.WorkOrderResponse;
import com.autoadda.apis.garage.util.JobCardStatus;
import com.autoadda.apis.garage.util.VehicleRecievedStatus;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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


    /**
     * Creates a new work order with the provided job cards.
     *
     * @param jobCards List of JobCard objects to be included in the work order.
     * @return The created WorkOrder object.
     */
    @Transactional
    public WorkOrder createWorkOrder(List<JobCard> jobCards) {
        WorkOrder workOrder = new WorkOrder ( );
        workOrder.setJobCards ( jobCards );
        workOrder.setStatus ( WorkOrderStatus.CREATED );
        workOrder.setCreatedDate ( LocalDateTime.now ( ) );
        return workOrderRepository.save ( workOrder );
    }

    public WorkOrderResponse createWorkOrder(WorkOrderRequest request) {

        WorkOrder workOrder = new WorkOrder ( );
        workOrder.setStatus ( WorkOrderStatus.CREATED );
        workOrder.setCreatedDate ( LocalDateTime.now ( ) );
        workOrder.setAppointment ( appointmentDetailsRepository.findById ( request.getAppointmentId ( ) ).orElseThrow ( () -> new ResourceNotFoundException ( "appointment not found" ) ) );
        workOrder.setGarage ( workOrder.getAppointment ( ).getGarage ( ) );
        workOrder = workOrderRepository.save ( workOrder );

        VehicleReceived byAppointmentId = vehicleReceivedRepository.findByAppointmentId ( workOrder.getAppointment ( ).getId ( ) );
        byAppointmentId.setStatus ( VehicleRecievedStatus.JOB_CARD_CREATED );
        vehicleReceivedRepository.saveAndFlush ( byAppointmentId );
        WorkOrderResponse workOrderResponse = workOrderMapper.entityToResponse ( workOrderRepository.save ( workOrder ) );

        List<JobCard> list = request.getAssignServiceRequestList ( ).stream ( ).map ( request1 -> jobCardService.assignTechnicianToServiceWithOutResponse ( request1, workOrderResponse.getId ( ) ) ).toList ( );
        workOrder.setJobCards ( list );

        WorkOrder finalWorkOrder = workOrder;
        list.forEach ( jobCard -> jobCard.setWorkOrder ( finalWorkOrder ) );
        String vehicleOwnerNumber = workOrderResponse.getAppointmentDetail (
        ).getVehicle ( ).getOwner ().getMobileNumber ( );
        String message = "Your work order has been created with ID: " + workOrderResponse.getId ( ) + ". Please visit your auto adda dashboard to proceed with the services.";
        GenericResponse genericResponse = authClient.sendSms ( new SmsRequest ( vehicleOwnerNumber, message ) );
        log.info ( genericResponse.getStatus ( ) );
        return workOrderResponse;

    }

    /**
     * Adds a job card to an existing work order.
     *
     * @param workOrderId The ID of the work order to which the job card will be
     *                    added.
     * @param jobCard     The JobCard object to be added.
     * @return The updated WorkOrder object.
     */
    @Transactional
    public WorkOrder getWorkOrderById(Integer id) {
        return workOrderRepository.findById ( id ).orElseThrow ( () -> new EntityNotFoundException ( "WorkOrder not found" ) );
    }

    /**
     * Retrieves all work orders with the specified status.
     *
     * @param status The status of the work orders to retrieve.
     * @return A list of WorkOrder objects with the specified status.
     */
    @Transactional
    public List<WorkOrder> getWorkOrdersByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatus ( status );
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
        WorkOrder workOrder = getWorkOrderById ( id );
        workOrder.updateStatus ( status );
        return workOrderRepository.save ( workOrder );
    }

    @Transactional
    public WorkOrder updateWorkOrderStatus(WorkOrder workOrder, WorkOrderStatus status) {
        workOrder.updateStatus ( status );
        return workOrderRepository.save ( workOrder );
    }

    /**
     * Retrieves all work orders associated with a specific garage.
     *
     * @param garageId The ID of the garage for which to retrieve work orders.
     * @return A list of WorkOrder objects associated with the specified garage.
     */
//	@Transactional
//	public List<WorkOrder> getWorkOrdersForGarage(Integer garageId) {
//		return workOrderRepository.findByJobCardGarageId(garageId);
//	}
    public List<WorkOrder> getWorkOrdersForGarage(Integer garageId) {
        return workOrderRepository.findByGarageId ( garageId );
    }

    public WorkOrderResponse getWorkOrderByAppointmentId(Integer appointmentId) {
        return workOrderMapper.entityToResponse ( workOrderRepository.findByAppointmentId ( appointmentId ).orElseThrow ( () -> new ResourceNotFoundException ( "work order not found" ) ) );
    }

    public List<WorkOrderResponse> getWorkOrdersByGarageId(Integer garageId) {
        return workOrderRepository.findByGarageId ( garageId ).stream ( ).map ( workOrderMapper::entityToResponse ).toList ( );
    }

    public WorkOrderResponse updateWorkOrder(Integer workOrderId, WorkOrderRequest request) {
        WorkOrder workOrder = workOrderRepository.findById ( workOrderId ).orElseThrow ( () -> new ResourceNotFoundException ( "no work order found with this id" ) );
        if ( request.getAssignServiceRequestList ( ) != null && ! request.getAssignServiceRequestList ( ).isEmpty ( ) ) {
            List<JobCard> jobCards = request.getAssignServiceRequestList ( ).stream ( ).map ( (AssignServiceRequest request1) -> jobCardService.assignTechnicianToServiceWithOutResponse ( request1, workOrder.getId ( ) ) ).toList ( );
            workOrder.getJobCards ( ).addAll ( jobCards );
            jobCards.forEach ( jobCard -> jobCard.setWorkOrder ( workOrder ) );
        }
        if ( request.getCustomerAcknowledged ( ) != null ) {
            workOrder.setCustomerAcknowledged ( request.getCustomerAcknowledged ( ) );
        }
        if ( request.getCustomerNegotiatedAmount ( ) != null ) {
            workOrder.setCustomerNegotiatedAmount ( request.getCustomerNegotiatedAmount ( )
                    //TODO: send sms to Garage about the updated negotiated amount
            );
        }
        if ( request.getCustomerNegotiatedComment ( ) != null ) {
            workOrder.setCustomerNegotiatedComment ( request.getCustomerNegotiatedComment ( ) );
        }
        if ( request.getGarageNegotiatedAmount ( ) != null ) {
            workOrder.setGarageNegotiatedAmount ( request.getGarageNegotiatedAmount ( ) );
            //TODO: send sms to Customer about the updated negotiated amount
        }
        if ( request.getGarageNegotiatedComment ( ) != null ) {
            workOrder.setGarageNegotiatedComment ( request.getGarageNegotiatedComment ( ) );
        }
        if ( request.getStatus ( ) != null ) {
            if ( ! workOrderStatusFlowService.isValidTransition ( workOrder.getStatus ( ), request.getStatus ( ) ) ) {
                throw new IllegalStateException ( "Invalid transition from " +
                        workOrder.getStatus ( ) + " to " + request.getStatus ( ) );
            }
            if ( request.getStatus ( ) == WorkOrderStatus.CONFIRMED ) {
                workOrder.getJobCards ( ).stream ( ).map ( JobCard::getId ).forEach ( id -> {
                    JobCard jobCard = jobCardRepository.findById ( id ).orElseThrow ( () -> new ResourceNotFoundException ( "no job card found with this id" ) );
                    if ( jobCard.getStatus ( ) == JobCardStatus.CREATED ) {
                        jobCardService.updateJobCardStatus ( jobCard.getId ( ), JobCardStatus.ASSIGNED );
                    }
                } );
            }
            workOrder.updateStatus ( request.getStatus ( ) );
        }

        return workOrderMapper.entityToResponse ( workOrderRepository.save ( workOrder ) );
    }

    public List<WorkOrderResponse> getWorkOrdersByCustomerId(Integer customerId) {
        return workOrderRepository.findByAppointmentCustomerId ( customerId ).stream ( ).map ( workOrderMapper::entityToResponse ).toList ( );
    }

    public WorkOrderResponse getWorkOrderResponseById(Integer workOrderId) {
        WorkOrder workOrder = getWorkOrderById ( workOrderId );
        return workOrderMapper.entityToResponse ( workOrder );
    }

    public void updateJobCardStatus(JobCard jobCard, JobCardStatus newStatus) {
        if ( ! statusFlowService.isValidTransition ( jobCard.getStatus ( ), newStatus ) ) {
            throw new IllegalStateException ( "Invalid transition from " +
                    jobCard.getStatus ( ) + " to " + newStatus );
        }
        JobCardStatus oldStatus = jobCard.getStatus ( );
        jobCard.setStatus ( newStatus );

        // Update timestamps based on status
        switch (newStatus) {
            case ASSIGNED:
                jobCard.setAssignedAt ( LocalDateTime.now ( ) );
                break;
            case IN_PROGRESS:
                if ( oldStatus == JobCardStatus.ASSIGNED ) {
                    jobCard.setStartedAt ( LocalDateTime.now ( ) );
                }
                break;
            case WORK_COMPLETED:
                jobCard.setCompletedAt ( LocalDateTime.now ( ) );
                break;
            case QUALITY_CHECK_PASSED, QUALITY_CHECK_FAILED:
                jobCard.setQualityCheckedAt ( LocalDateTime.now ( ) );
                break;
        }

        // Update WorkOrder status based on JobCard statuses
        workOrderStatusFlowService.updateWorkOrderStatusBasedOnJobCards ( jobCard.getWorkOrder ( ) );
    }


    public List<JobCard> getOverdueJobCards() {
        // Implementation to find overdue job cards
        return null; // TODO: implement
    }

    public void assignJobCard(JobCard jobCard, Integer technicianId) {
        // Set technician and update status
        updateJobCardStatus ( jobCard, JobCardStatus.ASSIGNED );
    }

    public void startJobCard(JobCard jobCard) {
        updateJobCardStatus ( jobCard, JobCardStatus.IN_PROGRESS );
    }

    public void completeJobCard(JobCard jobCard) {
        updateJobCardStatus ( jobCard, JobCardStatus.WORK_COMPLETED );
    }

    public void performQualityCheck(JobCard jobCard, boolean passed, String comments) {
        jobCard.setQualityComments ( comments );
        updateJobCardStatus ( jobCard, passed ?
                JobCardStatus.QUALITY_CHECK_PASSED : JobCardStatus.QUALITY_CHECK_FAILED );
    }
}
