package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.TechnicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfile, Integer> {

    Optional<TechnicianProfile> findByTechnicianId(Integer technicianId);
}
