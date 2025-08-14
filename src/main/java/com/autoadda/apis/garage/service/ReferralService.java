package com.autoadda.apis.garage.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.autoadda.apis.garage.entity.Referral;
import com.autoadda.apis.garage.repository.ReferralRepository;
import com.autoadda.apis.garage.util.ReferralUtil;
import com.autoadda.apis.garage.util.ReferredEntityType;
import com.autoadda.apis.garage.util.ReferrerType;

import jakarta.transaction.Transactional;

@Service
public class ReferralService {

	@Autowired
	private ReferralRepository referralRepository;

	/**
	 * Creates a referral code for a given referrer type and ID.
	 *
	 * @param type       The type of the referrer (e.g., USER, GARAGE).
	 * @param referrerId The ID of the referrer.
	 * @return The generated referral code.
	 */
	@Transactional
	public String createReferral(ReferrerType type, Integer referrerId) {
		String code = ReferralUtil.generateReferralCode(type.name(), referrerId);
		Referral referral = new Referral();
		referral.setReferralCode(code);
		referral.setReferrerType(type);
		referral.setReferrerId(referrerId);
		referral.setCreatedAt(LocalDateTime.now());
		referral.setRewardIssued(false);
		referralRepository.save(referral);
		return code;
	}

	/**
	 * Redeems a referral code for a referred entity.
	 *
	 * @param referralCode The referral code to redeem.
	 * @param referredId   The ID of the referred entity.
	 * @param referredType The type of the referred entity (e.g., USER, GARAGE).
	 */
	@Transactional
	public void redeemReferral(String referralCode, Integer referredId, ReferredEntityType referredType) {
		Referral referral = referralRepository.findByReferralCode(referralCode)
				.orElseThrow(() -> new RuntimeException("Invalid referral code"));

		referral.setReferredEntityId(referredId);
		referral.setReferredEntityType(referredType);
		referral.setRewardIssued(true); // could be a separate rewardService call
		referralRepository.save(referral);
	}
}
