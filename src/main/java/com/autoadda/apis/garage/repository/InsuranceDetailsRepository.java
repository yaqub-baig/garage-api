/**
 *
 */
package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.InsuranceDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceDetailsRepository extends JpaRepository<InsuranceDetails, Integer> {
}
