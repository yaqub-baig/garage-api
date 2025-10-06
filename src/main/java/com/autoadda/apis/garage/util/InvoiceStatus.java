package com.autoadda.apis.garage.util;

public enum InvoiceStatus {
    // Payment States
    PENDING,                    // Invoice created, payment pending
    PAID,                       // Full payment received

    // Completion States
    COMPLETED,                  // Invoice completed and closed

    // Exception States
    CANCELLED


}