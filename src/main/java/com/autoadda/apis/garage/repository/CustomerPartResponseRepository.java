package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.CustomerPartResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerPartResponseRepository extends JpaRepository<CustomerPartResponse, Integer> {

    Optional<CustomerPartResponse> findByPartRequestId(Integer partRequestId);
}
