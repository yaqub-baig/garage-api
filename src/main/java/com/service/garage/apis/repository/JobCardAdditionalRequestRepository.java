package com.service.garage.apis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.JobCardAdditionalRequest;

@Repository
public interface JobCardAdditionalRequestRepository extends JpaRepository<JobCardAdditionalRequest, Integer>{


}
