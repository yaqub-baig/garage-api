package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageTechnician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarageTechnicianRepository extends JpaRepository<GarageTechnician, Integer> {

    List<GarageTechnician> findByGarageId(Integer garageId);
}
