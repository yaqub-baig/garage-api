package com.autoadda.apis.garage.util;

public enum PartRequestStatus {
    // Initial State
    PENDING_CUSTOMER_RESPONSE,      // Waiting for customer to choose

    // Customer Decision States
    CUSTOMER_WILL_PROVIDE,          // Customer will provide the part
    GARAGE_WILL_PROCURE,            // Garage will buy the part

    // In Progress States
    GARAGE_PROCURING,               // Garage is purchasing the part
    WAITING_FOR_CUSTOMER_DELIVERY,  // Waiting for customer to deliver part

    // Completion States
    PART_RECEIVED,                  // Part received and ready to use
    ADDED_TO_INVOICE,               // Part cost added to invoice (for garage procurement)

    // Other States
    CANCELLED                       // Part request cancelled
}
