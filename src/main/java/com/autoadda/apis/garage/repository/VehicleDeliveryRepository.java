package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleDelivery;
import com.autoadda.apis.garage.util.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleDeliveryRepository extends JpaRepository<VehicleDelivery, Integer> {

    Optional<VehicleDelivery> findByWorkOrderId(Integer workOrderId);

    List<VehicleDelivery> findByStatus(DeliveryStatus status);

    List<VehicleDelivery> findByDeliveredById(Integer technicianId);
}
