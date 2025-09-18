package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.GarageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
