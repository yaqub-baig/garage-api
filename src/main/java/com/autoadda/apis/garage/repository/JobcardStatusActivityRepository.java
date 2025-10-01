package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.JobCardStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobcardStatusActivityRepository extends JpaRepository<JobCardStatusActivity, Integer> {


}
