package com.service.garage.apis.util;

import java.util.UUID;

public class ReferralUtil {
	public static String generateReferralCode(String prefix, Integer userId) {
		return prefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
	}
}
