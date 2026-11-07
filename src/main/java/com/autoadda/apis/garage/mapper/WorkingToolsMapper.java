package com.autoadda.apis.garage.mapper;

import lombok.RequiredArgsConstructor;
import com.autoadda.apis.garage.util.VoiceNoteHelper;
import com.autoadda.apis.garage.entity.WorkingTools;
import com.autoadda.apis.garage.response.WorkingToolResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingToolsMapper {

    private final VoiceNoteHelper voiceNoteHelper;

    public WorkingToolResponse entityToResponse(WorkingTools tool) {
        if (tool == null) {
            return null;
        }

        return WorkingToolResponse.builder()
                .id(tool.getId())
                .garageId(tool.getGarage() != null ? tool.getGarage().getId() : null)
                .toolName(tool.getToolName())
                .category(tool.getCategory())
                .brand(tool.getBrand())
                .model(tool.getModel())
                .quantity(tool.getQuantity())
                .condition(tool.getCondition())
                .isActive(tool.getIsActive())
                .createdAt(tool.getCreatedAt())
                .updatedAt(tool.getUpdatedAt())
                .notes(tool.getNotes())
                .voiceNote(voiceNoteHelper.toResponse(tool.getVoiceNote()))
                .toolImageId(tool.getToolImage() != null ? tool.getToolImage().getId() : null)
                .toolImageUrl(tool.getToolImage() != null ? tool.getToolImage().getFilePath() : null)
                .toolImageFileName(tool.getToolImage() != null ? tool.getToolImage().getFileName() : null)
                .build();
    }
}
