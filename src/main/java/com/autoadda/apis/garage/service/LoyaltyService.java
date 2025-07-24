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

	public LoyaltyPoint getLoyaltyStatus(Integer ownerId, Integer garageId) {
		return pointRepo.findByOwnerIdAndGarageId(ownerId, garageId)
				.orElseThrow(() -> new RuntimeException("Not enrolled in loyalty program"));
	}

	public List<LoyaltyTransaction> getTransactionHistory(Integer ownerId) {
		return txRepo.findByOwnerId(ownerId);
	}

	public List<LoyaltyTransaction> getTransactionHistoryByGarage(Integer ownerId, Integer garageId) {
		return txRepo.findByOwnerIdAndGarageId(ownerId, garageId);
	}

	public Optional<LoyaltyPoint> getAllLoyaltyPointsByGarage(Integer garageId) {
		return pointRepo.findByGarageId(garageId);
	}
}
