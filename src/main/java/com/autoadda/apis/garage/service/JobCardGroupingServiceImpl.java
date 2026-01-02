package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.exception.MemberNotFoundException;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.response.GroupedJobCardResponse;
import com.autoadda.apis.garage.response.JobCardResponse;
import com.autoadda.apis.garage.util.JobCardStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of job card grouping service.
 * Handles grouping of job cards by work order for both garage and technician views.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobCardGroupingServiceImpl implements JobCardGroupingService {

    private static final String UNKNOWN = "UNKNOWN";

    private final JobCardService jobCardService;
    private final WorkOrderRepository workOrderRepository;

    @Override
    @Transactional(readOnly = true)
    public GroupedJobCardResponse getGroupedJobCardsByWorkOrder(Integer garageId, JobCardStatus status) {
        List<JobCardResponse> jobCards = jobCardService.getGarageJobCards(garageId, status);
        return buildGroupedResponse(jobCards);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupedJobCardResponse getGroupedTechnicianJobCardsByWorkOrder(Integer technicianId, JobCardStatus status)
            throws MemberNotFoundException {
        List<JobCardResponse> jobCards = jobCardService.getTechnicianJobCards(technicianId, status);

        // Filter out CREATED status for technician view
        List<JobCardResponse> filteredJobCards = jobCards.stream()
                .filter(jobCard -> !Objects.equals(jobCard.getStatus(), JobCardStatus.CREATED.name()))
                .toList();

        return buildGroupedResponse(filteredJobCards);
    }

    /**
     * Builds a grouped response from a list of job cards.
     * Groups job cards by work order ID and includes work order status information.
     *
     * @param jobCards the list of job cards to group
     * @return GroupedJobCardResponse with job cards organized by work order
     */
    private GroupedJobCardResponse buildGroupedResponse(List<JobCardResponse> jobCards) {
        Map<Integer, GroupedJobCardResponse.WorkOrderJobCardGroup> workOrderGroups = jobCards.stream()
                .filter(jobCard -> jobCard.getWorkOrderId() != null)
                .collect(Collectors.groupingBy(
                        JobCardResponse::getWorkOrderId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createWorkOrderGroup
                        )
                ));

        // Handle job cards without work orders
        List<JobCardResponse> orphanedJobCards = jobCards.stream()
                .filter(jobCard -> jobCard.getWorkOrderId() == null)
                .toList();

        if (!orphanedJobCards.isEmpty()) {
            workOrderGroups.put(-1, createOrphanedGroup(orphanedJobCards));
        }

        return GroupedJobCardResponse.builder()
                .workOrderGroups(workOrderGroups)
                .totalJobCards(jobCards.size())
                .totalWorkOrders(workOrderGroups.size())
                .build();
    }

    /**
     * Creates a work order group from a list of job cards.
     *
     * @param jobCardList the list of job cards belonging to the same work order
     * @return WorkOrderJobCardGroup with work order details and job cards
     */
    private GroupedJobCardResponse.WorkOrderJobCardGroup createWorkOrderGroup(List<JobCardResponse> jobCardList) {
        Integer workOrderId = jobCardList.get(0).getWorkOrderId();

        try {
            WorkOrder workOrder = workOrderRepository.findById(workOrderId).orElse(null);
            return GroupedJobCardResponse.WorkOrderJobCardGroup.builder()
                    .workOrderId(workOrderId)
                    .jobCards(jobCardList)
                    .jobCardCount(jobCardList.size())
                    .workOrderStatus(workOrder != null ? workOrder.getStatus().toString() : UNKNOWN)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to fetch work order details for ID: {}", workOrderId, e);
            return GroupedJobCardResponse.WorkOrderJobCardGroup.builder()
                    .workOrderId(workOrderId)
                    .jobCards(jobCardList)
                    .jobCardCount(jobCardList.size())
                    .workOrderStatus(UNKNOWN)
                    .build();
        }
    }

    /**
     * Creates a special group for job cards without work orders.
     *
     * @param orphanedJobCards the list of job cards without work orders
     * @return WorkOrderJobCardGroup representing orphaned job cards
     */
    private GroupedJobCardResponse.WorkOrderJobCardGroup createOrphanedGroup(List<JobCardResponse> orphanedJobCards) {
        return GroupedJobCardResponse.WorkOrderJobCardGroup.builder()
                .workOrderId(-1)
                .jobCards(orphanedJobCards)
                .jobCardCount(orphanedJobCards.size())
                .workOrderStatus("NO_WORK_ORDER")
                .build();
    }
}
