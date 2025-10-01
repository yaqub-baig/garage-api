package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.WorkOrder;
import com.autoadda.apis.garage.util.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {
    List<WorkOrder> findByStatus(WorkOrderStatus status);

    List<WorkOrder> findByGarageId(Integer garageId);

    Optional<WorkOrder> findByAppointmentId(Integer appointmentId);

    List<WorkOrder> findByAppointmentCustomerId(Integer customerId);

}