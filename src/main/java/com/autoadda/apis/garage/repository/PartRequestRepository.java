package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.PartRequest;
import com.autoadda.apis.garage.util.PartRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartRequestRepository extends JpaRepository<PartRequest, Integer> {

    Optional<PartRequest> findByJobCardId(Integer jobCardId);

    List<PartRequest> findByCustomerId(Integer customerId);

    List<PartRequest> findByCustomerIdAndStatus(Integer customerId, PartRequestStatus status);

    List<PartRequest> findByStatus(PartRequestStatus status);

    List<PartRequest> findByJobCard_WorkOrder_Garage_Id(Integer garageId);

    List<PartRequest> findByJobCard_WorkOrder_Garage_IdAndStatus(Integer garageId, PartRequestStatus status);
}
