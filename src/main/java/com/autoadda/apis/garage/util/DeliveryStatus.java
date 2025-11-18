package com.autoadda.apis.garage.util;

public enum DeliveryStatus {
    PENDING_VERIFICATION,       // Garage preparing delivery checklist
    PENDING_CUSTOMER_APPROVAL,  // Waiting for customer to approve/reject
    APPROVED,                   // Customer approved delivery
    REJECTED                    // Customer rejected delivery - need to fix issues
}
