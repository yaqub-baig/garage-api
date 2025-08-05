package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.util.Status;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {
	List<WorkOrder> findByStatus(Status status);

	List<WorkOrder> findByGarageId(Integer garageId);
//	List<WorkOrder> findByJobCardGarageId(Integer garageId);
}