package com.service.garage.apis.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.service.garage.apis.entity.AvailableService;

@Repository
public interface AvailableServiceRepository extends JpaRepository<AvailableService, Integer> {

	Optional<AvailableService> findById(Integer id);

	List<AvailableService> findByNameContainingIgnoreCase(String name);

	List<AvailableService> findByDescriptionContainingIgnoreCase(String description);

	List<AvailableService> findByPriceBetween(Double minPrice, Double maxPrice);

	List<AvailableService> findByLaborPriceBetween(Double minLaborPrice, Double maxLaborPrice);

	List<AvailableService> findByNameContainingIgnoreCaseAndDescriptionContainingIgnoreCase(String name,
			String description);
}
