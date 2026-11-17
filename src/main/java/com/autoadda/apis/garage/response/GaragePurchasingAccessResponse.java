package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The garage as a parts buyer: who orders and where parts are delivered. Used by part-api. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GaragePurchasingAccessResponse {
    private Integer garageId;
    private String name;
    private String phone;
    private String email;
    private String trnNumber;
    /** Delivery address, one line. */
    private String address;
    private String city;
    private String mapLink;
}
