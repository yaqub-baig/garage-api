/**
 *
 */
package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRegistrationDetailsRepository extends JpaRepository<VehicleRegistration, Integer> {
}
