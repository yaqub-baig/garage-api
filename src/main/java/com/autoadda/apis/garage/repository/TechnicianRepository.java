package com.autoadda.apis.garage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autoadda.apis.garage.entity.Technician;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {

	Optional<Technician> findByEmailId(String email);

	Optional<Technician> findByMobileNumber(String phone);

	List<Technician> findByGarage_Id(Integer garageId);

	List<Technician> findBySkillSetContaining(String skill); // if skillSet is stored as a list

	List<Technician> findByStatus(String status);
}
