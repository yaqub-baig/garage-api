package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

    Optional<Technician> findByEmailId(String email);

    Optional<Technician> findByMobileNumber(String phone);

    List<Technician> findByGarageId(Integer garageId);

    @Query("SELECT t FROM Technician t JOIN t.skillSet s WHERE s.id = :skillId")
    List<Technician> findBySkillSetId(@Param("skillId") Integer skillId);

    List<Technician> findByStatus(String status);

    int countByGarageId(Integer id);

    Optional<Technician> findByAutoAddaId(Integer autoAddaId);
}
