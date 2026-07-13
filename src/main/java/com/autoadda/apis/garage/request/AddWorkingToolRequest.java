package com.autoadda.apis.garage.request;

import com.autoadda.apis.garage.util.ToolCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddWorkingToolRequest {
    private Integer garageId;
    private String toolName;
    private String category;
    private String brand;
    private String model;
    private Integer quantity;
    private ToolCondition condition;
    private String notes;
    private Long toolImageId;
}
