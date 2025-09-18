package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.PickupDropRequest;
import com.autoadda.apis.garage.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PickupDropRequestRepository extends JpaRepository<PickupDropRequest, Integer> {

    List<PickupDropRequest> findByGarageId(Integer garageId);

    List<PickupDropRequest> findByVehicleOwnerId(Integer vehicleOwnerId);

    List<PickupDropRequest> findByAssignedDriverId(Integer driverId);

    List<PickupDropRequest> findByStatus(Status status);

    List<PickupDropRequest> findByGarageIdAndStatus(Integer garageId, Status status);

    List<PickupDropRequest> findByScheduledTimeBetween(LocalDateTime start, LocalDateTime end);

    List<PickupDropRequest> findByVehicleOwnerIdAndStatus(Integer vehicleOwnerId, Status status);

    List<PickupDropRequest> findByAssignedDriverIdAndStatus(Integer driverId, Status status);
}
