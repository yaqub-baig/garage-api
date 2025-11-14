package com.autoadda.apis.garage.util;

public enum ItemProcurementStatus {
    PENDING_PROCUREMENT,    // Item is waiting to be procured by garage
    PROCURED,              // Item has been procured by garage
    CUSTOMER_WILL_PROVIDE, // Customer will provide this item
    CUSTOMER_PROVIDED      // Customer has provided this item
}
