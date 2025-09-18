package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.AuthenticationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthenticationDetailsRepository extends JpaRepository<AuthenticationDetails, Integer> {

    AuthenticationDetails findByUserName(String username);

}
