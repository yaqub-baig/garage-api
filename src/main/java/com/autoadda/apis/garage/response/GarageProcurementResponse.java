package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.PartCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageProcurementResponse {

    private Integer id;
    private String supplierName;
    private String supplierContact;
    private Double purchasePrice;
    private PartCondition partCondition;
    private String receiptNumber;
    private LocalDateTime procurementDate;
    private String procuredByUserName;
    private String notes;
}
