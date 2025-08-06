package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.GarageTechnician;

@Repository
public interface GarageTechnicianRepository extends JpaRepository<GarageTechnician, Integer> {

	List<GarageTechnician> findByGarageId(Integer garageId);
}
