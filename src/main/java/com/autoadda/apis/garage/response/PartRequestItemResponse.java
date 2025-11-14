package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ItemProcurementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartRequestItemResponse {

    private Integer id;
    private String partName;
    private String partNumber;
    private Integer quantity;
    private String description;
    private Double estimatedPrice;
    private ItemProcurementStatus procurementStatus;
    private Boolean addedToInvoice;
    private GarageProcurementResponse procurement;
}
