package com.service.garage.apis.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.GarageService;

@Repository
public interface GarageServiceRepository extends JpaRepository<GarageService, Integer> {

	List<GarageService> findByGarage_Id(Integer garageId);

	List<GarageService> findByIsActive(Boolean isActive);

	List<GarageService> findByIsActiveAndGarage_Id(Boolean isActive, Integer garageId);

	List<GarageService> findByAvailableService_NameContainingIgnoreCase(String name);

	List<GarageService> findByAvailableService_NameContainingIgnoreCaseAndIsActive(String name, Boolean isActive);

	List<GarageService> findByAvailableService_NameContainingIgnoreCaseAndIsActiveAndGarage_Id(String name,
			Boolean isActive, Integer garageId);

	List<GarageService> findByAvailableService_Id(Integer serviceId);

	List<GarageService> findByGarage_IdAndIsActive(Integer garageId, Boolean isActive);

	List<GarageService> findByGarage_IdAndAvailableService_Id(Integer garageId, Integer serviceId);
}
