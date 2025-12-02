package com.autoadda.apis.garage.util;

import java.util.UUID;

public class ReferralUtil {
    private ReferralUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String generateReferralCode(String prefix, Integer userId) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase() + userId;
    }
}
