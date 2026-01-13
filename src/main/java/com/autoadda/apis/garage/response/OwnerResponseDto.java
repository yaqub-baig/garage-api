package com.autoadda.apis.garage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnerResponseDto {
    private Integer id;
    private Integer responderId;
    private String responseText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
