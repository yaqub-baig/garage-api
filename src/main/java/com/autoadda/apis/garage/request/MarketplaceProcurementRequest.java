package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.PartCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Progress of a part request item bought through an AutoAdda marketplace order (sent by part-api).
 * ORDERED when the order is placed, CANCELLED when the order or the shop's item is cancelled,
 * RECEIVED when the garage receives it (creates the procurement and the invoice line).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketplaceProcurementRequest {

    public enum Stage { ORDERED, CANCELLED, RECEIVED }

    private Stage stage;
    private Integer orderId;
    private Long orderItemId;
    private String supplierName;
    private String supplierContact;
    /** Price per unit paid to the shop. */
    private Double unitPrice;
    private PartCondition condition;
}
