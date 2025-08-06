package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.garage.apis.entity.WorkOrder;
import com.service.garage.apis.util.Status;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {
	List<WorkOrder> findByStatus(Status status);

	List<WorkOrder> findByGarageId(Integer garageId);
//	List<WorkOrder> findByJobCardGarageId(Integer garageId);
}