package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageProcurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarageProcurementRepository extends JpaRepository<GarageProcurement, Integer> {

    Optional<GarageProcurement> findByPartRequestItemId(Integer partRequestItemId);

    List<GarageProcurement> findByPartRequestItem_PartRequest_JobCard_WorkOrder_Garage_Id(Integer garageId);
}
