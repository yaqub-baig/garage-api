package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.JobCard;
import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.repository.JobCardRepository;
import com.autoadda.apis.garage.repository.WorkOrderRepository;
import com.autoadda.apis.garage.util.Status;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

	private final WorkOrderRepository workOrderRepository;
	private final JobCardRepository jobCardRepository;

	public WorkOrder createWorkOrder(List<JobCard> jobCards) {
		WorkOrder workOrder = new WorkOrder();
		workOrder.setJobCards(jobCards);
		workOrder.setStatus(Status.CREATED);
		workOrder.setCreatedDate(LocalDateTime.now());

		return workOrderRepository.save(workOrder);
	}

	public WorkOrder getWorkOrderById(Integer id) {
		return workOrderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("WorkOrder not found"));
	}

	public List<WorkOrder> getWorkOrdersByStatus(Status status) {
		return workOrderRepository.findByStatus(status);
	}

	public WorkOrder updateWorkOrderStatus(Integer id, Status status) {
		WorkOrder workOrder = getWorkOrderById(id);
		workOrder.setStatus(status);
		return workOrderRepository.save(workOrder);
	}

	public List<WorkOrder> getWorkOrdersForGarage(Integer garageId) {
		return workOrderRepository.findByJobCardGarageId(garageId);
	}
}
