package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.CustomerChoice;
import com.autoadda.apis.garage.util.PartCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPartResponseResponse {

    private Integer id;
    private CustomerChoice choice;
    private PartCondition partCondition;
    private LocalDateTime responseDate;
    private LocalDate expectedDeliveryDate;
    private String customerComments;
}
