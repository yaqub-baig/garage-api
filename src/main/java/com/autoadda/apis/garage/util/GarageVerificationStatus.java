package com.autoadda.apis.garage.util;

public enum GarageVerificationStatus {
    PENDING,              // Awaiting admin review
    APPROVED,             // Admin approved, ready to activate
    REJECTED,             // Rejected by admin, needs fixes
    UNDER_REVIEW,         // Admin is actively reviewing
    ACTIVE,               // Approved and operational
    SUSPENDED,            // Temporarily suspended
    INACTIVE,             // Deactivated
    BLOCKED,              // Permanently blocked
    IMPORTED              // Scraped from external source, pending manual review
}
