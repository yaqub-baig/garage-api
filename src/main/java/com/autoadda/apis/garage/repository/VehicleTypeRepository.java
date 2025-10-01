/**
 *
 */
package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Integer> {
} 
