package com.service.garage.apis.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.service.garage.apis.entity.LoyaltyPoint;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Integer> {
	Optional<LoyaltyPoint> findByOwnerIdAndGarageId(Integer ownerId, Integer garageId);

	Optional<LoyaltyPoint> findByOwnerId(Integer ownerId);

	Optional<LoyaltyPoint> findByGarageId(Integer garageId);

	Optional<LoyaltyPoint> findByOwnerIdAndGarageIdAndPoints(Integer ownerId, Integer garageId, int points);
}