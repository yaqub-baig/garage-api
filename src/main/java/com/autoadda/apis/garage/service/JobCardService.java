package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.clients.AuthClient;
import com.autoadda.apis.garage.clients.VehicleClient;
import com.autoadda.apis.garage.entity.*;
import com.autoadda.apis.garage.exception.MemberNotFoundException;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.JobCardMapper;
import com.autoadda.apis.garage.repository.*;
import com.autoadda.apis.garage.request.AssignServiceRequest;
import com.autoadda.apis.garage.request.CreatePartRequestRequest;
import com.autoadda.apis.garage.request.JobCardStatusRequest;
import com.autoadda.apis.garage.request.SmsRequest;
import com.autoadda.apis.garage.response.GenericResponse;
import com.autoadda.apis.garage.response.PartRequestResponse;
import com.autoadda.apis.garage.response.GroupedJobCardResponse;
import com.autoadda.apis.garage.response.JobCardResponse;
import com.autoadda.apis.garage.response.VehicleDetailResponse;
import com.autoadda.apis.garage.util.BusinessValidator;
import com.autoadda.apis.garage.util.DateHelper;
import com.autoadda.apis.garage.util.JobCardStatus;
import com.autoadda.apis.garage.util.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobCardService {

    private final JobCardRepository jobCardRepo;
    private final JobcardStatusActivityRepository jobcardStatusActivityRepo;
    private final AppointmentServiceRepository appointmentServiceRepo;
    private final GarageRepository garageRepo;
    private final TechnicianRepository technicianRepo;
    private final AppointmentDetailsRepository appointmentRepo;
    private final JobCardMapper jobCardMapper;
    private final BusinessValidator validator;
    private final AuthClient authClient;
    private final VehicleClient vehicleClient;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusFlowService workOrderStatusFlowService;
    private final PartRequestService partRequestService;


    @Transactional
    public JobCardResponse assignTechnicianToService(AssignServiceRequest request) {
        AppointmentDetails appointment = appointmentRepo.findById ( request.getAppointmentId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Appointment not found" ) );
        log.info ( "3" );
        AppointmentServices service = appointmentServiceRepo.findById ( request.getServiceId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Service not found" ) );

        log.info ( "4" );
        Garage garage = garageRepo.findById ( request.getGarageId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Garage not found" ) );

        log.info ( "5" );
        Technician technician = technicianRepo.findById ( request.getTechnicianId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Technician not found" ) );

        log.info ( "6" );
        JobCard jobCard = JobCard.builder ( )
                .appointment ( appointment )
                .garage ( garage )
                .technician ( technician )
                .service ( service )
                .status ( JobCardStatus.ASSIGNED )
                .createdAt ( DateHelper.dbDate ( )
                        .atTime ( LocalTime.now ( ) ) )
                .comments ( request.getComments ( ) )
                .build ( );

        log.info ( "7" );
        jobCardRepo.save ( jobCard );

        jobcardStatusActivityRepo.save ( JobCardStatusActivity
                .builder ( )
                .jobcardId ( jobCard.getId ( ) )
                .status ( Status.ASSIGNED )
                .memberId ( request.getMemberId ( ) )
                .memberType ( request.getMemberType ( ) )
                .comments ( request.getComments ( ) )
                .insert_tms ( DateHelper.dbDate ( ) )
                .build ( ) );

        log.info ( "8" );
        service.setStatus ( Status.ASSIGNED );
        service.setTechnician ( technician );
        appointmentServiceRepo.save ( service );

        return jobCardMapper.entityToResponse ( jobCard );
    }

    @Transactional
    public JobCard assignTechnicianToServiceWithOutResponse(AssignServiceRequest request, Integer workOrderId) {
        AppointmentDetails appointment = appointmentRepo.findById ( request.getAppointmentId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Appointment not found" ) );

        AppointmentServices service = appointmentServiceRepo.findById ( request.getServiceId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Service not found" ) );

//		Garage garage = garageRepo.findById(request.getGarageId())
//				.orElseThrow(() -> new ResourceNotFoundException ("Garage not found"));
        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "WorkOrder not found" ) );
        Technician technician = technicianRepo.findById ( request.getTechnicianId ( ) )
                .orElseThrow ( () -> new ResourceNotFoundException ( "Technician not found" ) );

        JobCard jobCard = JobCard.builder ( )
                .appointment ( appointment )
                .garage ( appointment.getGarage ( ) )
                .technician ( technician )
                .service ( service )
                .status ( JobCardStatus.ASSIGNED )
                .createdAt ( DateHelper.dbDate ( )
                        .atTime ( LocalTime.now ( ) ) )
                .comments ( request.getComments ( ) )
                .workOrder ( workOrder )
                .build ( );

        jobCardRepo.save ( jobCard );

        jobcardStatusActivityRepo.save ( JobCardStatusActivity
                .builder ( )
                .jobcardId ( jobCard.getId ( ) )
                .status ( Status.ASSIGNED )
                .memberId ( request.getMemberId ( ) )
                .memberType ( request.getMemberType ( ) )
                .comments ( request.getComments ( ) )
                .insert_tms ( DateHelper.dbDate ( ) )
                .build ( ) );

        service.setStatus ( Status.ASSIGNED );
        service.setTechnician ( technician );
        appointmentServiceRepo.save ( service );

        return jobCard;
    }

    /**
     * Updates the status of a job card.
     *
     * @param request the request containing job card ID and new status
     * @return the updated JobCard
     */
    @Transactional
    public JobCardResponse updateJobCardStatus(Integer jobCardId, JobCardStatus request) {
        JobCard jobCard = jobCardRepo.findById ( jobCardId )
                .orElseThrow ( () -> new RuntimeException ( "Jobcard not found" ) );

        jobCard.updateStatus ( request );
        Integer appointmentId = jobCard.getService ( ).getAppointmentId ( );
        AppointmentDetails appointmentDetails = appointmentRepo.findById ( appointmentId ).orElseThrow ( () -> new ResourceNotFoundException ( "Appointment not found" ) );
        VehicleDetailResponse vehicleDetailsByIdResponse = vehicleClient.getVehicleDetailsByIdResponse ( appointmentDetails.getVehicleId ( ) );
        String vehicleOwnerNumber = vehicleDetailsByIdResponse.getOwner ( ).getMobileNumber ( );

        if ( JobCardStatus.IN_PROGRESS.equals ( request ) ) {
            jobCard.setServiceStartTime ( DateHelper.dbTime ( ) );
            String message = "Your work order service has started for " + jobCard.getService ( ).getService ( ).getAvailableService ( ).getName ( ) + "    at : " + LocalDateTime.now ( );
            GenericResponse genericResponse = authClient.sendSms ( new SmsRequest ( vehicleOwnerNumber, message ) );
            log.info ( genericResponse.getStatus ( ) );
        }
        if ( JobCardStatus.COMPLETED.equals ( request ) ) {
            jobCard.setServiceEndTime ( DateHelper.dbTime ( ) );
            jobCard.setCompletedAt ( LocalDateTime.now () );
            String message = "Your work order service has Completed for " + jobCard.getService ( ).getService ( ).getAvailableService ( ).getName ( ) + "    at : " + LocalDateTime.now ( );
            GenericResponse genericResponse = authClient.sendSms ( new SmsRequest ( vehicleOwnerNumber, message ) );
            log.info ( genericResponse.getStatus ( ) );
        }
        AppointmentServices service = jobCard.getService ( );

        appointmentServiceRepo.save ( service );

        // Update WorkOrder status based on JobCard statuses
        if ( jobCard.getWorkOrder ( ) != null ) {
            workOrderStatusFlowService.updateWorkOrderStatusBasedOnJobCards ( jobCard.getWorkOrder ( ) );
        }

        return jobCardMapper.entityToResponse ( jobCardRepo.save ( jobCard ) );
    }

    /**
     * Updates the status of a job card with part request details.
     * When status is WAITING_FOR_PARTS, part details must be provided.
     *
     * @param request the request containing job card ID, new status, and optional part details
     * @return the updated JobCard
     */
    @Transactional
    public JobCardResponse updateJobCardStatusWithPartRequest(JobCardStatusRequest request) {
        log.info("Updating job card status for jobCardId: {} to status: {}",
                request.getJobcardId(), request.getStatus());

        // Auto-set status to WAITING_FOR_PARTS if part items are provided but status is null
        if (request.getPartRequestItems() != null && !request.getPartRequestItems().isEmpty()) {
            if (request.getStatus() == null) {
                request.setStatus(JobCardStatus.WAITING_FOR_PARTS);
                log.info("Auto-setting status to WAITING_FOR_PARTS since part items are provided");
            }
        }

        // If status is WAITING_FOR_PARTS, validate that part details are provided
        if (request.getStatus() == JobCardStatus.WAITING_FOR_PARTS) {
            if (request.getPartRequestItems() == null || request.getPartRequestItems().isEmpty()) {
                throw new IllegalArgumentException(
                        "Part request items are required when status is WAITING_FOR_PARTS");
            }

            // Update the job card status first
            JobCardResponse jobCardResponse = updateJobCardStatus(request.getJobcardId(), request.getStatus());

            // Create or update part request
            CreatePartRequestRequest partRequest = CreatePartRequestRequest.builder()
                    .jobCardId(request.getJobcardId())
                    .technicianId(request.getMemberId())
                    .items(request.getPartRequestItems())
                    .notes(request.getPartRequestNotes())
                    .build();

            PartRequestResponse partRequestResponse = partRequestService.createPartRequest(partRequest);
            log.info("Part request created/updated with ID: {}", partRequestResponse.getId());

            return jobCardResponse;
        } else {
            // Validate status is not null for other cases
            if (request.getStatus() == null) {
                throw new IllegalArgumentException("Status must be provided");
            }
            // For other statuses, use the existing method
            return updateJobCardStatus(request.getJobcardId(), request.getStatus());
        }
    }

    /**
     * Retrieves a job card by its ID.
     *
     * @param jobCardId the ID of the job card
     * @return the JobCard if found
     */
    @Transactional(readOnly = true)
    public List<JobCardResponse> getTechnicianJobCards(Integer technicianId, JobCardStatus status) throws MemberNotFoundException {
        validator.validateTechnician ( technicianId );
        if ( status == null || StringUtils.isBlank ( status.toString ( ) ) )
            return jobCardRepo.findByTechnicianId ( technicianId ).stream ( ).map ( jobCardMapper::entityToResponse ).toList ( );
        else
            return jobCardRepo.findByTechnicianIdAndStatus ( technicianId, status ).stream ( ).map ( jobCardMapper::entityToResponse ).toList ( );
    }

    /**
     * Retrieves job cards for a specific garage.
     *
     * @param garageId the ID of the garage
     * @param status   the status of the job cards to filter by (optional)
     * @return a list of JobCards for the specified garage
     */
    @Transactional(readOnly = true)
    public List<JobCardResponse> getGarageJobCards(Integer garageId, JobCardStatus status) {
        if ( status == null || StringUtils.isBlank ( status.toString ( ) ) )
            return jobCardRepo.findByGarage_Id ( garageId ).stream ( ).map ( jobCardMapper::entityToResponse ).toList ( );
        else
            return jobCardRepo.findByGarage_IdAndStatus ( garageId, status ).stream ( ).map ( jobCardMapper::entityToResponse ).toList ( );
    }

    /**
     * Retrieves job cards for a specific appointment.
     *
     * @param appointmentId the ID of the appointment
     * @return a list of JobCards for the specified appointment
     */
    @Transactional(readOnly = true)
    public List<JobCard> getJobCardsByAppointmentId(Integer appointmentId) {
        return jobCardRepo.findByAppointmentId ( appointmentId );
    }

    /**
     * Marks a job card as completed.
     *
     * @param jobCardId the ID of the job card to mark as completed
     * @return the updated JobCard
     */
    @Transactional
    public JobCard markJobCardComplete(Integer jobCardId) {
        JobCard jobCard = jobCardRepo.findById ( jobCardId )
                .orElseThrow ( () -> new RuntimeException ( "JobCard not found with ID: " + jobCardId ) );

        if ( jobCard.getStatus ( ) == JobCardStatus.COMPLETED ) {
            throw new IllegalStateException ( "JobCard is already completed." );
        }

        jobCard.updateStatus ( JobCardStatus.COMPLETED );
        jobCard.setServiceEndTime ( LocalTime.now ( ) );

        return jobCardRepo.save ( jobCard );
    }

    @Transactional(readOnly = true)
    public JobCardResponse getJobCardById(Integer jobCardId) {
        JobCard jobCard = jobCardRepo.findById ( jobCardId )
                .orElseThrow ( () -> new ResourceNotFoundException ( "JobCard not found with ID: " + jobCardId ) );
        return jobCardMapper.entityToResponse ( jobCard );
    }

    /**
     * Retrieves job cards grouped by work order ID for a specific garage.
     *
     * @param garageId the ID of the garage
     * @param status   the status of the job cards to filter by (optional)
     * @return GroupedJobCardResponse with job cards organized by work order
     */
    @Transactional(readOnly = true)
    public GroupedJobCardResponse getGroupedJobCardsByWorkOrder(Integer garageId, JobCardStatus status) {
        List<JobCardResponse> jobCards = getGarageJobCards ( garageId, status );

        Map<Integer, GroupedJobCardResponse.WorkOrderJobCardGroup> workOrderGroups = jobCards.stream ( )
                .filter ( jobCard -> jobCard.getWorkOrderId ( ) != null )
                .collect ( Collectors.groupingBy (
                        JobCardResponse::getWorkOrderId,
                        Collectors.collectingAndThen (
                                Collectors.toList ( ),
                                jobCardList -> {
                                    Integer workOrderId = jobCardList.get ( 0 ).getWorkOrderId ( );
                                    try {
                                        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                                                .orElse ( null );
                                        return GroupedJobCardResponse.WorkOrderJobCardGroup.builder ( )
                                                .workOrderId ( workOrderId )
                                                .jobCards ( jobCardList )
                                                .jobCardCount ( jobCardList.size ( ) )
                                                .workOrderStatus ( workOrder != null ? workOrder.getStatus ( ).toString ( ) : "UNKNOWN" )
                                                .build ( );
                                    }
                                    catch ( Exception e ) {
                                        log.warn ( "Failed to fetch work order details for ID: {}", workOrderId, e );
                                        return GroupedJobCardResponse.WorkOrderJobCardGroup.builder ( )
                                                .workOrderId ( workOrderId )
                                                .jobCards ( jobCardList )
                                                .jobCardCount ( jobCardList.size ( ) )
                                                .workOrderStatus ( "UNKNOWN" )

                                                .build ( );
                                    }
                                }
                        )
                ) );

        // Handle job cards without work orders
        List<JobCardResponse> orphanedJobCards = jobCards.stream ( )
                .filter ( jobCard -> jobCard.getWorkOrderId ( ) == null )
                .toList ( );

        if ( ! orphanedJobCards.isEmpty ( ) ) {
            workOrderGroups.put ( - 1, GroupedJobCardResponse.WorkOrderJobCardGroup.builder ( )
                    .workOrderId ( - 1 )
                    .jobCards ( orphanedJobCards )
                    .jobCardCount ( orphanedJobCards.size ( ) )
                    .workOrderStatus ( "NO_WORK_ORDER" )

                    .build ( ) );
        }

        return GroupedJobCardResponse.builder ( )
                .workOrderGroups ( workOrderGroups )
                .totalJobCards ( jobCards.size ( ) )
                .totalWorkOrders ( workOrderGroups.size ( ) )
                .build ( );
    }

    /**
     * Retrieves job cards grouped by work order ID for a specific technician.
     *
     * @param technicianId the ID of the technician
     * @param status       the status of the job cards to filter by (optional)
     * @return GroupedJobCardResponse with job cards organized by work order
     */
    @Transactional(readOnly = true)
    public GroupedJobCardResponse getGroupedTechnicianJobCardsByWorkOrder(Integer technicianId, JobCardStatus status) throws MemberNotFoundException {
        List<JobCardResponse> jobCards = getTechnicianJobCards ( technicianId, status );

        Map<Integer, GroupedJobCardResponse.WorkOrderJobCardGroup> workOrderGroups = jobCards.stream ( )
                .filter ( jobCard -> jobCard.getWorkOrderId ( ) != null )
                .filter ( jobCard -> !Objects.equals ( jobCard.getStatus ( ), JobCardStatus.CREATED.name ( ) )  )
                        .collect ( Collectors.groupingBy (
                        JobCardResponse::getWorkOrderId,
                        Collectors.collectingAndThen (
                                Collectors.toList ( ),
                                jobCardList -> {
                                    Integer workOrderId = jobCardList.get ( 0 ).getWorkOrderId ( );
                                    try {
                                        WorkOrder workOrder = workOrderRepository.findById ( workOrderId )
                                                .orElse ( null );
                                        return GroupedJobCardResponse.WorkOrderJobCardGroup.builder ( )
                                                .workOrderId ( workOrderId )
                                                .jobCards ( jobCardList )
                                                .jobCardCount ( jobCardList.size ( ) )
                                                .workOrderStatus ( workOrder != null ? workOrder.getStatus ( ).toString ( ) : "UNKNOWN" )
                                                .build ( );
                                    }
                                    catch ( Exception e ) {
                                        log.warn ( "Failed to fetch work order details for ID: {}", workOrderId, e );
                                        return GroupedJobCardResponse.WorkOrderJobCardGroup.builder ( )
                                                .workOrderId ( workOrderId )
                                                .jobCards ( jobCardList )
                                                .jobCardCount ( jobCardList.size ( ) )
                                                .workOrderStatus ( "UNKNOWN" )

                                                .build ( );
                                    }
                                }
                        )
                ) );

        // Handle job cards without work orders
        List<JobCardResponse> orphanedJobCards = jobCards.stream ( )
                .filter ( jobCard -> jobCard.getWorkOrderId ( ) == null )
                .toList ( );

        if ( ! orphanedJobCards.isEmpty ( ) ) {
            workOrderGroups.put ( - 1, GroupedJobCardResponse.WorkOrderJobCardGroup.builder ( )
                    .workOrderId ( - 1 )
                    .jobCards ( orphanedJobCards )
                    .jobCardCount ( orphanedJobCards.size ( ) )
                    .workOrderStatus ( "NO_WORK_ORDER" )
                    .build ( ) );
        }

        return GroupedJobCardResponse.builder ( )
                .workOrderGroups ( workOrderGroups )
                .totalJobCards ( jobCards.size ( ) )
                .totalWorkOrders ( workOrderGroups.size ( ) )
                .build ( );
    }


}
