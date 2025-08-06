package com.service.garage.apis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.service.garage.apis.service.ReferralService;
import com.service.garage.apis.util.ReferredEntityType;
import com.service.garage.apis.util.ReferrerType;

@RestController
@RequestMapping("/api/referrals")
public class ReferralController {

	@Autowired
	private ReferralService referralService;

	@PostMapping("/generate")
	public ResponseEntity<String> generateReferral(@RequestParam ReferrerType type, @RequestParam Integer id) {
		String code = referralService.createReferral(type, id);
		return ResponseEntity.ok(code);
	}

	@PostMapping("/redeem")
	public ResponseEntity<Void> redeemReferral(@RequestParam String code, @RequestParam Integer newId,
			@RequestParam ReferredEntityType referredType) {
		referralService.redeemReferral(code, newId, referredType);
		return ResponseEntity.ok().build();
	}
}
