package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.PartCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageProcurementRequest {

    private Integer partRequestItemId;
    private String supplierName;
    private String supplierContact;
    private Double purchasePrice;
    private PartCondition partCondition;
    private String receiptNumber;
    private Integer procuredByUserId;
    private String notes;
}
