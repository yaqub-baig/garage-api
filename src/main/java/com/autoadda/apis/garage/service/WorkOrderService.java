package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

	@Autowired
	private final WorkOrderRepository workOrderRepository;

	@Autowired
	private final JobCardRepository jobCardRepository;

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
		workOrder.setStatus(Status.CREATED);
		workOrder.setCreatedDate(LocalDateTime.now());

		return workOrderRepository.save(workOrder);
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
		return workOrderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("WorkOrder not found"));
	}

	/**
	 * Retrieves all work orders with the specified status.
	 *
	 * @param status The status of the work orders to retrieve.
	 * @return A list of WorkOrder objects with the specified status.
	 */
	@Transactional
	public List<WorkOrder> getWorkOrdersByStatus(Status status) {
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
	public WorkOrder updateWorkOrderStatus(Integer id, Status status) {
		WorkOrder workOrder = getWorkOrderById(id);
		workOrder.setStatus(status);
		return workOrderRepository.save(workOrder);
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
}
