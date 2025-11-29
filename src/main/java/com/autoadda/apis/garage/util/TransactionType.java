package com.autoadda.apis.garage.util;

public enum TransactionType {
    STOCK_IN,           // Adding stock (purchase, transfer in)
    STOCK_OUT,          // Removing stock (job card usage, sale)
    ADJUSTMENT_IN,      // Inventory count increase correction
    ADJUSTMENT_OUT,     // Inventory count decrease correction
    DAMAGE,             // Stock damaged/spoiled
    RETURN_TO_SUPPLIER, // Returning defective parts
    TRANSFER_OUT,       // Transfer to another garage
    TRANSFER_IN         // Transfer from another garage
}
