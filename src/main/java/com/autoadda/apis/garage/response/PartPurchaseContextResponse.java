package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ItemProcurementStatus;
import com.autoadda.apis.garage.util.PartRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A part request item with its car and job, for ordering it from an AutoAdda shop (part-api). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartPurchaseContextResponse {
    private Integer garageId;
    private Integer workOrderId;
    private Integer jobCardId;
    private Integer partRequestId;
    private Integer itemId;
    private String partName;
    private String partNumber;
    private Integer quantity;
    private String description;
    private PartRequestStatus partRequestStatus;
    private ItemProcurementStatus procurementStatus;
    /** True when the customer agreed the garage buys it and the item isn't bought yet. */
    private boolean purchasable;
    private Integer marketplaceOrderId;
    private Long marketplaceOrderItemId;
    private Integer vehicleId;
    /** Vehicle names as stored in vehicle-api (null when vehicle-api is unavailable). */
    private String brand;
    private String model;
    private String year;
}
