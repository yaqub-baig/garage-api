package com.autoadda.apis.garage.service;

import com.autoadda.apis.garage.entity.Garage;
import com.autoadda.apis.garage.entity.MediaFile;
import com.autoadda.apis.garage.entity.WorkingTools;
import com.autoadda.apis.garage.exception.ResourceNotFoundException;
import com.autoadda.apis.garage.mapper.WorkingToolsMapper;
import com.autoadda.apis.garage.repository.GarageRepository;
import com.autoadda.apis.garage.repository.MediaFileRepository;
import com.autoadda.apis.garage.repository.WorkingToolsRepository;
import com.autoadda.apis.garage.request.AddWorkingToolRequest;
import com.autoadda.apis.garage.request.UpdateWorkingToolRequest;
import com.autoadda.apis.garage.response.WorkingToolResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkingToolsService {

    public static final String TOOL_NOT_FOUND = "Working tool not found";

    private final WorkingToolsRepository workingToolsRepo;
    private final GarageRepository garageRepo;
    private final MediaFileRepository mediaFileRepository;
    private final WorkingToolsMapper mapper;

    /**
     * Add a new tool/equipment entry to a garage
     */
    @Transactional
    public WorkingToolResponse addTool(AddWorkingToolRequest request) {
        log.info("Adding working tool for garage: {}", request.getGarageId());

        Garage garage = garageRepo.findById(request.getGarageId())
                .orElseThrow(() -> new ResourceNotFoundException("Garage not found with ID: " + request.getGarageId()));

        if (request.getToolName() == null || request.getToolName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tool name is required");
        }
        if (request.getQuantity() != null && request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be greater than or equal to 0");
        }

        MediaFile toolImage = resolveToolImage(request.getToolImageId());

        WorkingTools tool = WorkingTools.builder()
                .garage(garage)
                .toolName(request.getToolName())
                .category(request.getCategory())
                .brand(request.getBrand())
                .model(request.getModel())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .condition(request.getCondition())
                .notes(request.getNotes())
                .toolImage(toolImage)
                .build();

        tool = workingToolsRepo.save(tool);
        log.info("Working tool created with ID: {}", tool.getId());

        return mapper.entityToResponse(tool);
    }

    /**
     * Get a specific tool by ID
     */
    public WorkingToolResponse getTool(Integer toolId) {
        log.info("Fetching working tool: {}", toolId);

        WorkingTools tool = workingToolsRepo.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException(TOOL_NOT_FOUND));

        return mapper.entityToResponse(tool);
    }

    /**
     * Get all active tools for a garage
     */
    public List<WorkingToolResponse> getGarageTools(Integer garageId) {
        log.info("Fetching working tools for garage: {}", garageId);

        List<WorkingTools> tools = workingToolsRepo.findByGarageIdAndIsActiveTrue(garageId);

        return tools.stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Update tool details
     */
    @Transactional
    public WorkingToolResponse updateTool(Integer toolId, UpdateWorkingToolRequest request) {
        log.info("Updating working tool: {}", toolId);

        WorkingTools tool = workingToolsRepo.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException(TOOL_NOT_FOUND));

        if (request.getQuantity() != null && request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be greater than or equal to 0");
        }

        if (request.getToolName() != null) {
            tool.setToolName(request.getToolName());
        }
        if (request.getCategory() != null) {
            tool.setCategory(request.getCategory());
        }
        if (request.getBrand() != null) {
            tool.setBrand(request.getBrand());
        }
        if (request.getModel() != null) {
            tool.setModel(request.getModel());
        }
        if (request.getQuantity() != null) {
            tool.setQuantity(request.getQuantity());
        }
        if (request.getCondition() != null) {
            tool.setCondition(request.getCondition());
        }
        if (request.getNotes() != null) {
            tool.setNotes(request.getNotes());
        }
        if (request.getToolImageId() != null) {
            tool.setToolImage(resolveToolImage(request.getToolImageId()));
        }

        tool = workingToolsRepo.save(tool);
        log.info("Working tool updated: {}", toolId);

        return mapper.entityToResponse(tool);
    }

    /**
     * Search tools for a garage by name, category or brand
     */
    public List<WorkingToolResponse> searchTools(Integer garageId, String searchTerm) {
        log.info("Searching working tools for garage: {} with term: {}", garageId, searchTerm);

        List<WorkingTools> tools = workingToolsRepo.searchWorkingTools(garageId, searchTerm);

        return tools.stream()
                .map(mapper::entityToResponse)
                .toList();
    }

    /**
     * Deactivate tool (soft delete)
     */
    @Transactional
    public void deactivateTool(Integer toolId) {
        log.info("Deactivating working tool: {}", toolId);

        WorkingTools tool = workingToolsRepo.findById(toolId)
                .orElseThrow(() -> new ResourceNotFoundException(TOOL_NOT_FOUND));

        tool.setIsActive(false);
        workingToolsRepo.save(tool);

        log.info("Working tool deactivated: {}", toolId);
    }

    private MediaFile resolveToolImage(Long toolImageId) {
        if (toolImageId == null) {
            return null;
        }
        return mediaFileRepository.findById(toolImageId).orElse(null);
    }
}
