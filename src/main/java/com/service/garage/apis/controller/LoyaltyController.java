package com.service.garage.apis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.entity.LoyaltyPoint;
import com.service.garage.apis.entity.LoyaltyTransaction;
import com.service.garage.apis.service.LoyaltyService;

@RestController
@RequestMapping("/loyalty")
public class LoyaltyController {

	@Autowired
	private LoyaltyService loyaltyService;

	@PostMapping("/earn")
	public ResponseEntity<String> earnPoints(@RequestParam Integer ownerId, @RequestParam Integer garageId,
			@RequestParam int points, @RequestParam String reason) {
		loyaltyService.addPoints(ownerId, garageId, points, reason);
		return ResponseEntity.ok("Points added");
	}

	@PostMapping("/redeem")
	public ResponseEntity<String> redeemPoints(@RequestParam Integer ownerId, @RequestParam Integer garageId,
			@RequestParam int points) {
		loyaltyService.redeemPoints(ownerId, garageId, points);
		return ResponseEntity.ok("Points redeemed");
	}

	@GetMapping("/balance")
	public ResponseEntity<LoyaltyPoint> getPoints(@RequestParam Integer ownerId, @RequestParam Integer garageId) {
		return ResponseEntity.ok(loyaltyService.getLoyaltyStatus(ownerId, garageId));
	}

	@GetMapping("/history")
	public ResponseEntity<List<LoyaltyTransaction>> getHistory(@RequestParam Integer ownerId) {
		return ResponseEntity.ok(loyaltyService.getTransactionHistory(ownerId));
	}

	@GetMapping("/history/garage/{garageId}/owner/{ownerId}")
	public ResponseEntity<List<LoyaltyTransaction>> getHistoryByGarage(@RequestParam Integer garageId,
			@RequestParam(required = false) Integer ownerId) {
		return ResponseEntity.ok(loyaltyService.getTransactionHistoryByGarage(garageId, ownerId));
	}

}
