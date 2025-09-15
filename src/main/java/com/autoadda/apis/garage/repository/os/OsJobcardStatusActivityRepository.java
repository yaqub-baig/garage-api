package com.autoadda.apis.garage.repository.os;

import com.autoadda.apis.garage.entity.os.OsJobcardStatusActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.JobCardStatusActivity;

@Repository
public interface OsJobcardStatusActivityRepository extends JpaRepository<OsJobcardStatusActivity, Integer>{



}
