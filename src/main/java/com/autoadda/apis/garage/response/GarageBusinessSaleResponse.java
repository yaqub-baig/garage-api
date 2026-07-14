package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarageBusinessSaleResponse {
    private Integer id;
    private Integer listingId;
    private Integer buyerId;
    private Double salePrice;
    private String paymentMode;
    private LocalDate saleDate;
    private String notes;
}
