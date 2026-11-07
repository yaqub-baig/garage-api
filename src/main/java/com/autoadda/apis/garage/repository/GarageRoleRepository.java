package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarageRoleRepository extends JpaRepository<GarageRole, Integer> {

    List<GarageRole> findByGarageIdOrderByIdAsc(Integer garageId);

    Optional<GarageRole> findByGarageIdAndNameIgnoreCase(Integer garageId, String name);

    Optional<GarageRole> findByIdAndGarageId(Integer id, Integer garageId);

    boolean existsByGarageId(Integer garageId);
}
