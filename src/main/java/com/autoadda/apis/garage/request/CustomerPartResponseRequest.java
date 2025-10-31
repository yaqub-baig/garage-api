package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.CustomerChoice;
import com.autoadda.apis.garage.util.PartCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPartResponseRequest {

    private CustomerChoice choice;
    private PartCondition partCondition; // Required if choice is GARAGE_PROCURE
    private LocalDate expectedDeliveryDate; // Optional, for SELF_PROVIDE
    private String customerComments;
}
