package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.JobCardAdditionalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobCardAdditionalRequestRepository extends JpaRepository<JobCardAdditionalRequest, Integer> {


}
