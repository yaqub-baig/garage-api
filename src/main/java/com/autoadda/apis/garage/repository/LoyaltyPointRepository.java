package com.autoadda.apis.garage.repository;

import com.autoadda.apis.garage.entity.LoyaltyPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, Integer> {
    Optional<LoyaltyPoint> findByOwnerIdAndGarageId(Integer ownerId, Integer garageId);

    Optional<LoyaltyPoint> findByOwnerId(Integer ownerId);

    Optional<LoyaltyPoint> findByGarageId(Integer garageId);

    Optional<LoyaltyPoint> findByOwnerIdAndGarageIdAndPoints(Integer ownerId, Integer garageId, int points);
}