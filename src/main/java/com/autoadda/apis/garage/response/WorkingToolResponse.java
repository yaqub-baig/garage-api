package com.autoadda.apis.garage.response;

import com.autoadda.apis.garage.util.ToolCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingToolResponse {
    private Integer id;
    private Integer garageId;
    private String toolName;
    private String category;
    private String brand;
    private String model;
    private Integer quantity;
    private ToolCondition condition;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String notes;

    // Tool image information
    private Long toolImageId;
    private String toolImageUrl;
    private String toolImageFileName;
}
