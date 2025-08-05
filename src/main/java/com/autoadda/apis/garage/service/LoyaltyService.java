package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.LoyaltyPoint;
import com.autoadda.apis.garage.entity.LoyaltyTransaction;
import com.autoadda.apis.garage.entity.VehicleOwner;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.LoyaltyPointRepository;
import com.autoadda.apis.garage.repository.LoyaltyTransactionRepository;
import com.autoadda.apis.garage.repository.VehicleOwnerRepository;

import jakarta.transaction.Transactional;

@Service
public class LoyaltyService {

	@Autowired
	private LoyaltyPointRepository pointRepo;

	@Autowired
	private GarageRepository garageRepo;

	@Autowired
	private VehicleOwnerRepository ownerRepo;

	@Autowired
	private LoyaltyTransactionRepository txRepo;

	/**
	 * Adds points to a vehicle owner's loyalty account for a specific garage.
	 *
	 * @param ownerId      the ID of the vehicle owner
	 * @param garageId     the ID of the garage
	 * @param earnedPoints the number of points to be added
	 * @param reason       the reason for adding points
	 */
	@Transactional
	public void addPoints(Integer ownerId, Integer garageId, int earnedPoints, String reason) {
		LoyaltyPoint lp = pointRepo.findByOwnerIdAndGarageId(ownerId, garageId).orElseGet(() -> {
			LoyaltyPoint newLp = new LoyaltyPoint();

			// Ensure the owner exists before setting it
			VehicleOwner vehilceOwner = ownerRepo.findById(ownerId)
					.orElseThrow(() -> new RuntimeException("Vehicle Owner not found"));
			newLp.setOwner(vehilceOwner);

			// Ensure the garage exists before setting it
			Garage garage = garageRepo.findById(garageId).orElseThrow(() -> new RuntimeException("Garage not found"));

			newLp.setGarage(garage);
			newLp.setPoints(0);
			return newLp;
		});

		lp.setPoints(lp.getPoints() + earnedPoints);
		lp.setLastUpdated(LocalDateTime.now());
		pointRepo.save(lp);

		LoyaltyTransaction tx = new LoyaltyTransaction();
		// Ensure the owner exists before setting it
		VehicleOwner owner = ownerRepo.findById(ownerId)
				.orElseThrow(() -> new RuntimeException("Vehicle Owner not found"));
		// Ensure the garage exists before setting it
		Garage garage = garageRepo.findById(garageId).orElseThrow(() -> new RuntimeException("Garage not found"));
		tx.setOwner(owner);
		tx.setGarage(garage);
		tx.setPointsChanged(earnedPoints);
		tx.setDescription(reason);
		tx.setTransactionDate(LocalDateTime.now());
		txRepo.save(tx);
	}

	/**
	 * Redeems points from a vehicle owner's loyalty account for a specific garage.
	 *
	 * @param ownerId      the ID of the vehicle owner
	 * @param garageId     the ID of the garage
	 * @param redeemPoints the number of points to be redeemed
	 */
	@Transactional
	public void redeemPoints(Integer ownerId, Integer garageId, int redeemPoints) {
		LoyaltyPoint lp = pointRepo.findByOwnerIdAndGarageId(ownerId, garageId)
				.orElseThrow(() -> new RuntimeException("No points record found"));

		if (lp.getPoints() < redeemPoints) {
			throw new RuntimeException("Insufficient points");
		}

		lp.setPoints(lp.getPoints() - redeemPoints);
		lp.setRedeemedPoints(lp.getRedeemedPoints() + redeemPoints);
		lp.setLastUpdated(LocalDateTime.now());
		pointRepo.save(lp);

		LoyaltyTransaction tx = new LoyaltyTransaction();
		// Ensure the owner exists before setting it
		VehicleOwner owner = ownerRepo.findById(ownerId)
				.orElseThrow(() -> new RuntimeException("Vehicle Owner not found"));
		tx.setOwner(owner);
		// Ensure the garage exists before setting it
		Garage garage = garageRepo.findById(garageId).orElseThrow(() -> new RuntimeException("Garage not found"));
		tx.setGarage(garage);
		tx.setPointsChanged(-redeemPoints);
		tx.setDescription("Points Redeemed");
		tx.setTransactionDate(LocalDateTime.now());
		txRepo.save(tx);
	}

	/**
	 * Retrieves the loyalty status of a vehicle owner for a specific garage.
	 *
	 * @param ownerId  the ID of the vehicle owner
	 * @param garageId the ID of the garage
	 * @return the LoyaltyPoint object containing loyalty status
	 */
	@Transactional
	public LoyaltyPoint getLoyaltyStatus(Integer ownerId, Integer garageId) {
		return pointRepo.findByOwnerIdAndGarageId(ownerId, garageId)
				.orElseThrow(() -> new RuntimeException("Not enrolled in loyalty program"));
	}

	/**
	 * Retrieves the transaction history of a vehicle owner.
	 *
	 * @param ownerId the ID of the vehicle owner
	 * @return a list of LoyaltyTransaction objects
	 */
	@Transactional
	public List<LoyaltyTransaction> getTransactionHistory(Integer ownerId) {
		return txRepo.findByOwnerId(ownerId);
	}

	/**
	 * Retrieves the transaction history of a vehicle owner for a specific garage.
	 *
	 * @param ownerId  the ID of the vehicle owner
	 * @param garageId the ID of the garage
	 * @return a list of LoyaltyTransaction objects
	 */
	@Transactional
	public List<LoyaltyTransaction> getTransactionHistoryByGarage(Integer ownerId, Integer garageId) {
		return txRepo.findByOwnerIdAndGarageId(ownerId, garageId);
	}

	/**
	 * Retrieves all loyalty points for a specific garage.
	 *
	 * @param garageId the ID of the garage
	 * @return an Optional containing LoyaltyPoint if found, otherwise empty
	 */
	@Transactional
	public Optional<LoyaltyPoint> getAllLoyaltyPointsByGarage(Integer garageId) {
		return pointRepo.findByGarageId(garageId);
	}
}
