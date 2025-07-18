package com.autoadda.apis.garage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.AuthenticationDetails;

@Repository
public interface AuthenticationDetailsRepository extends JpaRepository<AuthenticationDetails, Integer>{

	AuthenticationDetails findByUserName(String username);

}
