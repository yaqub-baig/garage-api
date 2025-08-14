package com.autoadda.apis.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.JobCardStatusActivity;

@Repository
public interface JobcardStatusActivityRepository extends JpaRepository<JobCardStatusActivity, Integer>{

	

}
