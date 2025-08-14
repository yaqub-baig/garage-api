package com.autoadda.apis.garage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autoadda.apis.garage.entity.LoyaltyTransaction;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Integer> {
	List<LoyaltyTransaction> findByOwnerId(Integer ownerId);

	List<LoyaltyTransaction> findByGarageId(Integer garageId);

	List<LoyaltyTransaction> findByOwnerIdAndGarageId(Integer ownerId, Integer garageId);

	List<LoyaltyTransaction> findByOwnerIdAndGarageIdAndPointsChanged(Integer ownerId, Integer garageId,
			int pointsChanged);

	List<LoyaltyTransaction> findByOwnerIdAndGarageIdAndDescription(Integer ownerId, Integer garageId,
			String description);
}