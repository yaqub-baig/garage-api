package com.autoadda.apis.garage.mapper;

import com.autoadda.apis.garage.entity.RequestingGarageAndTechnician;
import com.autoadda.apis.garage.response.RequestingGarageTechnicianResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestingGarageTechnicianMapper {

    private final GarageMapper garageMapper;
    private final TechnicianMapper technicianMapper;

    /**
     * Convert entity to response DTO
     * Maps the full entity graph including nested garage and technician details
     */
    public RequestingGarageTechnicianResponse entityToResponse(RequestingGarageAndTechnician entity) {
        if (entity == null) {
            return null;
        }

        return RequestingGarageTechnicianResponse.builder()
                .id(entity.getId())
                .garage(entity.getGarage() != null
                        ? garageMapper.entityToResponse(entity.getGarage())
                        : null)
                .technician(entity.getTechnician() != null
                        ? technicianMapper.entityToResponse(entity.getTechnician())
                        : null)
                .sendBy(entity.getSendBy())
                .approvalStatus(entity.getApprovalStatus())
                .requestedAt(entity.getRequestedAt())
                .description(entity.getDescription())
                .build();
    }
}
