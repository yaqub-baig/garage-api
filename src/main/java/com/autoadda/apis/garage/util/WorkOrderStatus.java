package com.autoadda.apis.garage.util;

public enum WorkOrderStatus {
    // Initial States
    CREATED,                    // Work order just created
    NEGOTIATION,                //negotiation with customer ongoing
    CONFIRMED,                  // Customer confirmed the work order

    // Work States
    ASSIGNED,                   // Assigned to technician
    WORK_IN_PROGRESS,          // Work has started
    WAITING_FOR_SPARES,        // Waiting for parts/spares
    JOB_COMPLETED,             // All work completed

    // Quality & Review States
    PENDING_REVIEW,            // Quality check pending
    READY_FOR_DELIVERY,        // Ready to be delivered to customer

    // Invoice & Payment States
    INVOICE_INITIATED,         // Invoice generation started
    INVOICED,                  // Invoice generated
    PENDING_PAYMENT,           // Waiting for customer payment
    PAYMENT_RECEIVED,          // Payment completed

    // Delivery States
    DELIVERED_TO_CUSTOMER,     // Vehicle delivered to customer
    COMPLETED,                 // Work order fully completed

    // Exception States
    ON_HOLD,                   // Work temporarily stopped
    CANCELLED,                 // Work order cancelled
    REJECTED                   // Work order rejected
}